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

import jakarta.persistence.Tuple;

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
				.select("transactionid,office,voucherdate,bankacname,bankacno").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> update() {
		
		if (!auth.hasPermission("*")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		
		DbResponse rowEffect;
		BankVoucher model = new BankVoucher();
		model.loadData(document);
		String sql = "select count(id) from "+table+" where transactionid=? and bankvoucherno!=null";
		Tuple t = db.getSingleResult(sql,Arrays.asList(model.transactionid));
		if((t.get(0)+"").equals("0")) {
			return Messenger.getMessenger().setMessage("Already submitted voucher").error();
		}
		 sql = "UPDATE " + table + " set depositdate=?,bankvoucherno=?,remarks=? where transactionid=?";
		System.out.println(sql);
		rowEffect = db.execute(sql, Arrays.asList(model.depositdate,model.bankvoucherno,model.remarks, model.transactionid));
		//System.out.println(rowEffect.getErrorNumber());
		if (rowEffect.getErrorNumber() == 0) {
			
			try {
				JSONObject resp =  api.updateToSutra(model.transactionid,model.bankvoucherno,model.depositdate,model.remarks);
				if(resp!=null) {
					if(resp.getInt("status")==1) {
						db.execute("update bank_deposits set syncstatus=2 where transactionid='"+model.transactionid+"'");
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
		String sql = "select fyid,transactionid,officename,collectioncenterid,lgid,voucherdate,voucherdateint,bankid,accountnumber,amount from " + table + " where transactionid=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid));
		if(data==null) {
			JSONObject dt =  api.getTransDetails(transactionid);
			if(dt!=null) {
				try {
					if(dt.getInt("status")==1) {
						JSONObject d = dt.getJSONObject("data");
						db.execute("insert into "+table+" (id,fyid,transactionid,officename,collectioncenterid,lgid,voucherdate,voucherdateint,bankid,accountnumber,amount) values (?,?,?,?,?,?,?,?,?,?,?)",Arrays.asList(d.get("id"),d.get("fyid"),d.get("transactionid"),d.get("officename"),d.get("collectioncenterid"),d.get("lgid"),d.get("voucherdate"),d.get("voucherdateint"),d.get("bankid"),d.get("accountnumber"),d.get("amount")));
						return Messenger.getMessenger().setData(d.toMap()).success();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return Messenger.getMessenger().error();
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