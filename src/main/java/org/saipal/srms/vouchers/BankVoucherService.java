package org.saipal.srms.vouchers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.ApiManager;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.persistence.Tuple;

@Component
public class BankVoucherService extends AutoService {

	@Autowired
	Authenticated auth;

	@Autowired
	ApiManager api;

	private String table = "bank_deposits";

	
	public ResponseEntity<Map<String, Object>> index() {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where id!=1 ";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = TaxPayerVoucher.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
		  condition += ")";
			
		}
		String sort = "";
		if (!request("sortKey").isBlank()) {
			if (!request("sortDir").isBlank()) {
				sort = request("sortKey") + " " + request("sortDir");
			}
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("transactionid,officename,voucherdate,accountnumber, amount").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> update() {
		
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		BankVoucher model = new BankVoucher();
		model.loadData(document);
		String usq = "select count(bankvoucherno) from "+table+" where bankvoucherno=? and bankid=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.bankvoucherno,auth.getBankId()));
		if ((!(res.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("This voucherno is already in use.").error();
		}
		String sql = "select count(id) from "+table+" where transactionid=? and (bankvoucherno is null or bankvoucherno=0)";
		Tuple t = db.getSingleResult(sql,Arrays.asList(model.transactionid));
		if((t.get(0)+"").equals("0")) {
			return Messenger.getMessenger().setMessage("Already submitted voucher").error();
		}
		String amount = model.amount;
		String actualAmount = db.getSingleResult("select amount from "+table+" where transactionid=?",Arrays.asList(model.transactionid)).get(0)+"";
		if(Float.parseFloat(amount)!=Float.parseFloat(actualAmount)) {
			return Messenger.getMessenger().setMessage("Deposited amount and Voucher amount does not match.").error();
		}
		 sql = "UPDATE " + table + " set depositdate=?,bankvoucherno=?,remarks=?,creatorid=?,approverid=?,approved=1 where transactionid=? and bankid=?";
		 rowEffect = db.execute(sql, Arrays.asList(model.depositdate,model.bankvoucherno,model.remarks,auth.getUserId(),auth.getUserId(),model.transactionid,auth.getBankId()));
		//System.out.println(rowEffect.getErrorNumber());
		if (rowEffect.getErrorNumber() == 0) {
			try {
				JSONObject resp =  api.updateToSutra(model.transactionid,model.bankvoucherno,model.depositdate,model.remarks);
				if(resp!=null) {
					if(resp.getInt("status")==1) {
						db.execute("update "+table+" set syncstatus=2 where transactionid=? and bankid=?",Arrays.asList(model.transactionid,auth.getBankId()));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String,Object>> getTransDetails() {
		
		String transactionid = request("transactionid");
		if(transactionid.isBlank()) {
			return Messenger.getMessenger().setMessage("Transaction id is required").error();
		}
		String sql = "select bd.fyid,bd.trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,bd.voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from " + table + " bd join bankaccount ba on ba.accountnumber=bd.accountnumber  where transactionid=? and bd.bankid=? and (bd.bankvoucherno=0 OR bd.bankvoucherno is null)";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid,auth.getBankId()));
//		if(data.ge)
		if(data==null) {
			JSONObject dt =  api.getTransDetails(transactionid);
//			System.out.println(dt.toString());
			if(dt!=null) {
				try {
					if(dt.getInt("status")==1) {
						JSONObject d = dt.getJSONObject("data");
						db.execute("insert into "+table+" (id,fyid,transactionid,officename,collectioncenterid,lgid,voucherdate,voucherdateint,bankid,accountnumber,amount) values (?,?,?,?,?,?,?,?,?,?,?)",Arrays.asList(d.get("id"),d.get("fyid"),d.get("transactionid"),d.get("officename"),d.get("collectioncenterid"),d.get("lgid"),d.get("voucherdate"),d.get("voucherdateint"),d.get("bankid"),d.get("accountnumber"),d.get("amount")));
						sql = "select bd.fyid,bd.trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,bd.voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from " + table + " bd join bankaccount ba on ba.accountnumber=bd.accountnumber  where transactionid=? and bankid=?";
						Map<String, Object> fdata = db.getSingleResultMap(sql, Arrays.asList(transactionid,auth.getBankId()));
						return Messenger.getMessenger().setData(fdata).success();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return Messenger.getMessenger().setMessage("No such transaction found.").error();
		}
		return Messenger.getMessenger().setData(data).success();
	}
	
	
	/*
	 * To Be Called by SuTRA application, to get the deposit voucher details 
	 * using the payment reference number
	 * */
	public ResponseEntity<Map<String,Object>> getVoucherStatus() {
		String transactionid = request("transactionid");
		if(transactionid.isBlank()) {
			return Messenger.getMessenger().setMessage("Transaction id is required").error();
		}
		String sql = "select transactionid,bankvoucherno,depositdate,remarks,status from " + table + " where transactionid=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid));
		return Messenger.getMessenger().setData(data).success();
	}
	
	

	

}