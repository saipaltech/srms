package org.saipal.srms.vouchers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
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
import javax.transaction.Transactional;

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
			List<String> searchbles = BankVoucher.searchables();
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
		condition = condition+ " and depositbranchid="+auth.getBranchId()+" and depositbankid="+auth.getBankId()+" ";
		if (!auth.canFromUserTable("4")) {
			condition += " and deposituserid='"+auth.getUserId()+"'";
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("transactionid,officename,cast(depositdate as date) as depositdate ,accountnumber, amount").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> chequeDeposit() throws JSONException {
		String items=request("selection");
		if (!items.startsWith("[")) {
			items = "[" + items + "]";
		}
		JSONArray jarr = new JSONArray(items);
		try {
			if (jarr.length() > 0) {
				for (int i = 0; i < jarr.length(); i++) {
				String usq="select * from chequeBankDakhilaDetail where did=?";	
				Tuple res = db.getSingleResult(usq, Arrays.asList(jarr.get(i)));
				String sq="select * from chequeBankDakhilaMain where cdid=?";
				Tuple rs = db.getSingleResult(sq, Arrays.asList(res.get("mainid")));
				
				String sq1="select count(id) as cid from taxvouchers where cref=?";
				Tuple rs1 = db.getSingleResult(sq1, Arrays.asList(res.get("did")));
				if(!rs1.get("cid").toString().equals("1")) {
					String sql="insert into taxvouchers(cref,dateint,bankid,branchid,karobarsanket,chequeno,chequeamount,cstatus,chequebank,lgid,date,taxpayername,bankorgid,amountcr,ttype,depositbankid,depositbranchid,deposituserid,depositedby) values(?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					DbResponse rf= db.execute(sql,Arrays.asList(res.get("did"),auth.getBankId(),auth.getBranchId(),res.get("ksno"),res.get("chequeno"),res.get("chequeamount"),0,res.get("bankid"),rs.get("orgid"),new Date(),res.get("taxpayername"),rs.get("bankorgid"),res.get("chequeamount"),2,auth.getBankId(),auth.getBranchId(),auth.getUserId(),res.get("taxpayername")));
//					System.out.println(rf.getMessage());
					String squ="update chequeBankDakhilaDetail set isbankreceived=?,bankreceivedby=?,bankreceiveddate=? where did=?";
					db.execute(squ,Arrays.asList(1,auth.getBankId(),new Date(),jarr.get(i)));
				}
				
				
				
				}
				return Messenger.getMessenger().success();
			}else {
				return Messenger.getMessenger().setMessage("No Data to save").error();
			}
		} catch (Exception e) {
			return Messenger.getMessenger().setMessage("Unable to save data").error();
		}
		
		
	}

	public ResponseEntity<Map<String, Object>> update() {
		
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		BankVoucher model = new BankVoucher();
		model.loadData(document);
		String usq = "select amount,usestatus from "+table+" where transactionid=? and bankid=? and paymentmethod=2";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.transactionid,auth.getBankId()));
		if(res!=null) {
			if(!(res.get("usestatus")+"").equals("0")) {
				return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
			}
			if(Float.parseFloat(model.amount)!=Float.parseFloat(res.get("amount")+"")) {
				return Messenger.getMessenger().setMessage("Deposited amount and Voucher amount does not match.").error();
			}
			String sql = "UPDATE " + table + " set depositdate=?,depositdateint=format(getdate(),'yyyyMMdd'),bankvoucherno=?,remarks=?,deposituserid=?,approverid=?,approved=1, depositbankid=?,depositbranchid=?,usestatus=1 where transactionid=? and bankid=?";
			 rowEffect = db.execute(sql, Arrays.asList(model.depositdate,model.bankvoucherno,model.remarks,auth.getUserId(),auth.getUserId(),auth.getBankId(), auth.getBranchId(),model.transactionid,auth.getBankId()));
			if (rowEffect.getErrorNumber() == 0) {
				try {
					JSONObject resp =  api.updateToSutra(model.transactionid,model.bankvoucherno,model.depositdate,model.remarks,"1");
					if(resp!=null) {
						if(resp.getInt("status")==1) {
							db.execute("update "+table+" set syncstatus=2 where transactionid=? and bankid=?",Arrays.asList(model.transactionid,auth.getBankId()));
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				return Messenger.getMessenger().success();
			} else {
				return Messenger.getMessenger().error();
			}
		}
		return Messenger.getMessenger().setMessage("No such transaction found.").error();
	}

	public ResponseEntity<Map<String,Object>> getTransDetails() {
		
		String transactionid = request("transactionid");
		if(transactionid.isBlank()) {
			return Messenger.getMessenger().setMessage("Transaction id is required").error();
		}
		transactionid = nep2EngNum(transactionid);
		if(transactionid.length()< 8) {
			return Messenger.getMessenger().setMessage("Invalid Transaction id format.").error();
		}
		char forthChar = transactionid.charAt(3);
		if(forthChar == '9') {
			return getTransDetailsCheque();
		}

		String sql = "select usestatus,bd.fyid,bd.trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,bd.voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from " + table + " bd join bankaccount ba on ba.accountnumber=bd.accountnumber  where transactionid=? and bd.bankid=? and bd.paymentmethod=2";
		//String sql = "select bd.fyid,bd.trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,cast(bd.voucherdate as date) as voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from " + table + " bd join bankaccount ba on ba.accountnumber=bd.accountnumber  where transactionid=? and bd.bankid=? and bd.paymentmethod=2";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid,auth.getBankId()));
		if(data==null) {
			JSONObject dt =  api.getTransDetails(transactionid);
			if(dt!=null) {
				try {
					if(dt.getInt("status")==1) {
						JSONObject d = dt.getJSONObject("data");
						db.execute("insert into "+table+" (id,fyid,transactionid,officename,collectioncenterid,lgid,voucherdate,voucherdateint,bankid,accountnumber,amount,usestatus) values (?,?,?,?,?,?,?,?,?,?,?)",Arrays.asList(d.get("id"),d.get("fyid"),d.get("transactionid"),d.get("officename"),d.get("collectioncenterid"),d.get("lgid"),d.get("voucherdate"),d.get("voucherdateint"),d.get("bankid"),d.get("accountnumber"),d.get("amount"),d.get("usestatus")));
						sql = "select bd.usestatus,bd.fyid,bd.trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,cast(bd.voucherdate as date) as voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from " + table + " bd join bankaccount ba on ba.accountnumber=bd.accountnumber  where transactionid=? and bankid=? and bd.paymentmethod=2";
						Map<String, Object> fdata = db.getSingleResultMap(sql, Arrays.asList(transactionid,auth.getBankId()));
						if(!(fdata.get("usestatus")+"").equals("0")) {
							return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
						}
						return Messenger.getMessenger().setData(fdata).success();
					}else {
						return Messenger.getMessenger().setMessage(dt.getString("message")).error();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			return Messenger.getMessenger().setMessage("No such transaction found.").error();
		}
		if(!(data.get("usestatus")+"").equals("0")) {
			return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
		}
		return Messenger.getMessenger().setData(data).success();
	}
	
	@Transactional
	private ResponseEntity<Map<String, Object>> getTransDetailsCheque() {
		String transactionid = request("transactionid"); 
		transactionid = nep2EngNum(transactionid);
		String sql = "select bd.fyid,bd.trantype,bd.cdid,bd.karobarSanketNo,bd.orgid as lgid,bd.trandate,bd.trandatetint,bd.bankid,bd.accountno,ba.accountname,bi.namenp as bankname,ll.namenp as palika from chequeBankDakhilaMain bd join bankaccount ba on ba.id=bd.bankorgid join bankinfo bi on bi.id=bd.bankid join admin_local_level_structure ll on ll.id=bd.orgid where karobarSanketNo=? and bd.bankid=? ";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid,auth.getBankId()));
		if(data==null) {
			JSONObject dt =  api.getChequeDetails(transactionid);
			if(dt!=null) {
				try {
					if(dt.getInt("status")==1) {
//						System.out.println("here");
						JSONObject d = dt.getJSONObject("data");
						DbResponse rf= db.execute("insert into chequeBankDakhilaMain (cdid ,adminid ,orgid ,fyid ,trantype ,karobarSanketNo ,trandate ,trandatetint ,refNo ,narration ,bankorgid ,bankid ,accountno ,entrydate ,enterby) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
								Arrays.asList(d.get("cdid"),d.get("adminid"),d.get("orgid"),d.get("fyid"),d.get("trantype"),d.get("karobarsanketno"),d.get("trandate"),d.get("trandatetint"),d.get("refno"),d.get("narration"),d.get("bankorgid"),d.get("bankid"),d.get("accountno"),d.get("entrydate"),d.get("enterby")));
						
						 JSONArray dtls=d.getJSONArray("details_rows");
						
						if(dtls.length()>0) {
							for(int i=0;i<dtls.length();i++) {
								JSONObject dd = dtls.getJSONObject(i);
								db.execute("insert into chequeBankDakhilaDetail(did ,mainid ,rcid ,ksno ,bankid ,chequeno ,chequeamount ,taxpayername ) values(?,?,?,?,?,?,?,?)",
										Arrays.asList(dd.get("did"),dd.get("mainid"),dd.get("rcid"),dd.get("ksno"),dd.get("bankid"),dd.get("chequeno"),dd.get("chequeamount"),dd.get("taxpayername")));
							
							}
						}
						sql = "select bd.fyid,bd.trantype,bd.cdid,bd.karobarSanketNo,bd.orgid as lgid,bd.trandate,bd.trandatetint,bd.bankid,bd.accountno,ba.accountname,bi.namenp as bankname,ll.namenp as palika from chequeBankDakhilaMain bd join bankaccount ba on ba.id=bd.bankorgid join bankinfo bi on bi.id=bd.bankid join admin_local_level_structure ll on ll.id=bd.orgid where karobarSanketNo=? and bd.bankid=?";
						Map<String, Object> fdata = db.getSingleResultMap(sql, Arrays.asList(transactionid,auth.getBankId()));
						String sqld="select cast(cb.did as varchar) as did ,cb.mainid ,cb.rcid ,cb.ksno ,cb.bankid ,cb.chequeno ,cb.chequeamount ,cb.taxpayername ,cb.isbankreceived ,cb.bankreceivedby ,cb.bankreceiveddate,bi.namenp as bankname from chequeBankDakhilaDetail cb join bankinfo bi on bi.id=cb.bankid where mainid=?";
						List <Map<String, Object>> dtl = db.getResultListMap(sqld, Arrays.asList(fdata.get("cdid")));
						fdata.put("details", dtl);
						return Messenger.getMessenger().setData(fdata).success();
					}else {
						return Messenger.getMessenger().setMessage(dt.getString("message")).error();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return Messenger.getMessenger().setMessage("No such transaction found.").error();
		}
		String sqld="select cast(cb.did as varchar) as did ,cb.mainid ,cb.rcid ,cb.ksno ,cb.bankid ,cb.chequeno ,cb.chequeamount ,cb.taxpayername ,cb.isbankreceived ,cb.bankreceivedby ,cb.bankreceiveddate,bi.namenp as bankname from chequeBankDakhilaDetail cb join bankinfo bi on bi.id=cb.bankid where mainid=? and isbankreceived=?";
		List <Map<String, Object>> dtl = db.getResultListMap(sqld, Arrays.asList(data.get("cdid"),0));
		data.put("details", dtl);
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
		String sql = "select transactionid,bankvoucherno,depositdate,remarks,usestatus from " + table + " where transactionid=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid));
		return Messenger.getMessenger().setData(data).success();
	}

	public ResponseEntity<Map<String, Object>> saveBankVoucher() {
		return getTransDetails();
	}
	
	

	

}