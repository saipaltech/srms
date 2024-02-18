package org.saipal.srms.vouchers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
		condition = condition + " and depositbranchid=" + auth.getBranchId() + " and depositbankid=" + auth.getBankId()
				+ " ";
		if (!auth.canFromUserTable("4")) {
			condition += " and deposituserid='" + auth.getUserId() + "'";
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("transactionid,officename,cast(depositdate as date) as depositdate ,accountnumber, amount")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	@Transactional
	public ResponseEntity<Map<String, Object>> reconcilation() throws JSONException {
		String id=request("id");
		String status=request("status");
		String remarks=request("remarks");
		String approvedate=request("approvedate");
		String approvedby=request("approvedby");
		if(status.equals("1")) {
			// voucher modify
		}
		String sql="update tblreconcilation set approvestatus=?,rejectremarks=?,approvedate=?,approvedby=? where id=?";
		DbResponse rs = db.execute(sql,
				Arrays.asList(status, remarks, approvedate,approvedby,id));
		if (rs.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			} else {
			return Messenger.getMessenger().setMessage(rs.getMessage()).error();
		}
    }
	
	@Transactional
	public ResponseEntity<Map<String, Object>> vouchercancel() throws JSONException {
		VoucherCancel model = new VoucherCancel();
		model.loadData(document);
		String usq = "select count(id) as total from tblreconcilation where sutrasanket=? and banksanket=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.sksno, model.bksno));
		if (res != null) {
			if (!(res.get("total") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("Transaction already been submitted.").error();
			}
		}
		if(!model.sutralgid.equals(model.banklgid)) {
			return Messenger.getMessenger().setMessage("Palika not matched").error();
		}
		
		if(!model.sutraamount.equals(model.bankamount)) {
			return Messenger.getMessenger().setMessage("Amount not matched").error();
		}
		
		DbResponse rs = db.execute("insert into tblreconcilation (lgid,collectioncenterid,bankid,branchid,sutrasanket,banksanket,sutraamount,bankamount,remarks,requestby) values (?,?,?,?,?,?,?,?,?,?)",
				Arrays.asList(model.sutralgid,model.sutraccid,auth.getBankId(),auth.getBranchId(),model.sksno,model.bksno,model.sutraamount,model.bankamount,model.remarks,auth.getUserId()));
		if (rs.getErrorNumber() == 0) {
			
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().setMessage(rs.getMessage()).error();
		}
		
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> chequeDeposit() throws JSONException {
		String items = request("selection");
		if (items.length() == 0) {
			return Messenger.getMessenger().setMessage("No Data to save").error();
		}
		if (!items.startsWith("[")) {
			items = "[" + items + "]";
		}
		try {
			JSONArray jarr = new JSONArray(items);
			if (jarr.length() > 0) {
				String qry = "select STRING_AGG(ksno,',') as karobarsankets from chequeBankDakhilaDetail where did in ("
						+ (items.replace("[", "").replace("]", "").replace("\"", "'")) + ")";
				Tuple t = db.getSingleResult(qry);
				String karobarsanket = t.get("karobarsankets") + "";
				JSONObject resp = api.chequeReceived(karobarsanket, auth.getBankId());
				if (resp != null) {
					if (resp.getInt("status") == 1) {
						for (int i = 0; i < jarr.length(); i++) {
							String usq = "select * from chequeBankDakhilaDetail where did=?";
							Tuple res = db.getSingleResult(usq, Arrays.asList(jarr.get(i)));
							String sq = "select * from chequeBankDakhilaMain where cdid=?";
							Tuple rs = db.getSingleResult(sq, Arrays.asList(res.get("mainid")));

							String sq1 = "select count(id) as cid from taxvouchers where cref=?";
							Tuple rs1 = db.getSingleResult(sq1, Arrays.asList(res.get("did")));
							if (!rs1.get("cid").toString().equals("1")) {
								karobarsanket += res.get("ksno") + ",";
								String sql = "insert into taxvouchers(cref,dateint,bankid,branchid,karobarsanket,chequeno,chequeamount,cstatus,chequebank,lgid,collectioncenterid,date,taxpayername,bankorgid,amountcr,ttype,depositbankid,depositbranchid,deposituserid,depositedby) values(?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,getdate(),?,?,?,?,?,?,?,?)";
								DbResponse rf = db.execute(sql,
										Arrays.asList(res.get("did"), auth.getBankId(), auth.getBranchId(),
												res.get("ksno"), res.get("chequeno"), res.get("chequeamount"), 0,
												res.get("bankid"), rs.get("adminid"),rs.get("orgid"), res.get("taxpayername"),
												rs.get("bankorgid"), res.get("chequeamount"), 2, auth.getBankId(),
												auth.getBranchId(), auth.getUserId(), res.get("taxpayername")));
								// System.out.println(rf.getMessage());
								String usqq = "select * from taxvouchers where cref=?";
								Tuple resq = db.getSingleResult(usqq, Arrays.asList(res.get("did")));
								String sqq = "INSERT INTO taxvouchers_detail (did,mainid,revenueid,amount) values(?,?,?,?)";
								db.execute(sqq, Arrays.asList(db.newIdInt(), resq.get("id"), res.get("revid"),
										res.get("chequeamount")));
								String squ = "update chequeBankDakhilaDetail set isbankreceived=?,bankreceivedby=?,bankreceiveddate=? where did=?";
								db.execute(squ, Arrays.asList(1, auth.getBankId(), new Date(), jarr.get(i)));
							}
						}
						return Messenger.getMessenger().success();
					}
				}
				return Messenger.getMessenger().setMessage("Unable to update to sutra").error();
			} else {
				return Messenger.getMessenger().setMessage("No Data to save").error();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Messenger.getMessenger().setMessage("Unable to save data").error();
		}

	}
	@Transactional
	public ResponseEntity<Map<String, Object>> updateforportal() throws JSONException {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		BankVoucher model = new BankVoucher();
		model.loadData(document);
		String usq = "select amountcr,isused from taxvouchers where karobarsanket=? and bankid=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.transactionid, auth.getBankId()));
		if (res != null) {
			if (!(res.get("isused") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
			}
			if (Float.parseFloat(model.amount) != Float.parseFloat(res.get("amountcr") + "")) {
				return Messenger.getMessenger().setMessage("Deposited amount and Voucher amount does not match.")
						.error();
			}
			String sql = "UPDATE  taxvouchers set date=?,dateint=format(getdate(),'yyyyMMdd'),voucherno=?,deposituserid=?,ttype=1, depositbankid=?,depositbranchid=?,isused=1 where karobarsanket=? and bankid=?";
			rowEffect = db.execute(sql,
					Arrays.asList(model.depositdate, model.bankvoucherno, auth.getUserId(),
							 auth.getBankId(), auth.getBranchId(), model.transactionid,
							auth.getBankId()));
			
			boolean autoVerify = false;
			Tuple u = db.getSingleResult("select id,amountlimit,permid from users where id=?",
					Arrays.asList(auth.getUserId()));
			if ((u.get("permid") + "").equals("3")) {
				if ((u.get("amountlimit") + "").equals("-1")
						|| (Float.parseFloat(model.amount) <= Float.parseFloat(u.get("amountlimit") + ""))) {
					autoVerify = true;
				}
			}
			if ((u.get("permid") + "").equals("4")) {
				autoVerify = true;
			}
			if (autoVerify) {
				JSONObject resp = api.updateToSutra(model.transactionid, model.bankvoucherno,
						model.depositdate, model.remarks, "1");
				
				
				System.out.println(resp);
				if (resp != null) {
					if (resp.getInt("status") == 1) {
						db.execute("update taxvouchers set approved=1,updatedon=getdate(),approverid=? where karobarsanket=?",
								Arrays.asList(auth.getUserId(), model.transactionid));
						db.execute("update taxvouchers set syncstatus=2 where karobarsanket=?",
								Arrays.asList(model.transactionid));
						return Messenger.getMessenger().success();
					}else {
						return Messenger.getMessenger().setData(resp).error();
					}
				
				
				}
				else {
					return Messenger.getMessenger().setMessage("Internal Error").error();
				}
				
			}
			return Messenger.getMessenger().success();
			
		}else {
			return Messenger.getMessenger().setMessage("Data Not found").error();
		}
//		return null;
	}

	public ResponseEntity<Map<String, Object>> update() throws JSONException {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		BankVoucher model = new BankVoucher();
		model.loadData(document);
		char forthChar = model.transactionid.charAt(3);
		if (forthChar == '2') {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		if (forthChar == '4' || forthChar == '5') {
			return updateforportal();
		}
		String usq = "select amount,usestatus from " + table + " where transactionid=? and bankid=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.transactionid, auth.getBankId()));
		if (res != null) {
			if (!(res.get("usestatus") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
			}
			if (Float.parseFloat(model.amount) != Float.parseFloat(res.get("amount") + "")) {
				return Messenger.getMessenger().setMessage("Deposited amount and Voucher amount does not match.")
						.error();
			}
			try {
				JSONObject lockResp = api.getLock(model.transactionid);
				if (lockResp != null) {
					if (lockResp.getInt("status") == 1) {
						String sql = "UPDATE " + table
								+ " set depositdate=?,depositdateint=format(getdate(),'yyyyMMdd'),bankvoucherno=?,remarks=?,deposituserid=?,approverid=?,approved=1, depositbankid=?,depositbranchid=?,usestatus=1 where transactionid=? and bankid=?";
						rowEffect = db.execute(sql,
								Arrays.asList(model.depositdate, model.bankvoucherno, model.remarks, auth.getUserId(),
										auth.getUserId(), auth.getBankId(), auth.getBranchId(), model.transactionid,
										auth.getBankId()));
						if (rowEffect.getErrorNumber() == 0) {
							db.execute("insert into sync_karobars (karobarsanket) values(?)",
									Arrays.asList(model.transactionid));
							JSONObject resp = api.updateToSutra(model.transactionid, model.bankvoucherno,
									model.depositdate, model.remarks, "1");
							if (resp != null) {
								if (resp.getInt("status") == 1) {
									db.execute("update " + table + " set syncstatus=2 where transactionid=?",
											Arrays.asList(model.transactionid));
									db.execute("delete from sync_karobars where karobarsanket=?",
											Arrays.asList(model.transactionid));
								}
							}
							return Messenger.getMessenger().success();
						} else {
							return Messenger.getMessenger().setMessage("Internal Error").error();
						}
					} else {
						return Messenger.getMessenger().setMessage(lockResp.getString("message")).error();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Messenger.getMessenger().setMessage("Unable to connect to SuTRA.").error();
	}
	
	
	public ResponseEntity<List<Map<String, Object>>> getreconcilation() {
		String adminid=request("adminid");
		String sql = "select sutrasanket,banksanket,cast(lgid as varchar) as lgid,cast(collectioncenterid as varchar) as collectioncenterid,cast(bankid as varchar)as bankid,cast(branchid as varchar) as branchid,sutraamount,bankamount,remarks,requestdate,requestby from tblreconcilation where approvestatus=0 and lgid="+adminid;
		return ResponseEntity.ok(db.getResultListMap(sql));
	}
	
	public ResponseEntity<Map<String, Object>> submitToPalika() {
		String id=request("id");
		DbResponse rowEffect = db.execute("update tblreconcilation set approvestatus=1 where id="+id);
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().setMessage("Submitted").success();
		} else {
			return Messenger.getMessenger().setMessage("could not submit").error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> deleteVoucher() {
		String id=request("id");
		DbResponse rowEffect = db.execute("delete from  tblreconcilation where id="+id);
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().setMessage("deleted").success();
		} else {
			return Messenger.getMessenger().setMessage("could not delete").error();
		}
	}
	public ResponseEntity<Map<String, Object>> getTransDetails() {
		String transactionid = request("transactionid");
		transactionid = nep2EngNum(transactionid).trim();
		if (!isKarobarsanketValid(transactionid)) {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		char forthChar = transactionid.charAt(3);
		if (forthChar == '2') {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		if (forthChar == '9') {
			return getTransDetailsCheque();
		}
		if (forthChar == '4' || forthChar == '5') {
			return getTransDetailsRmisPortal();
		}
		String sql = "select usestatus from " + table + " where transactionid=? and bankid=? and paymentmethod=2";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid, auth.getBankId()));
		if (data != null) {
			if (!(data.get("usestatus") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
			}
		}
		JSONObject dt = api.getTransDetails(transactionid);
		if (dt != null) {
			try {
				if (dt.getInt("status") == 1) {
					if (data != null) {
						sql = "select bd.usestatus,bd.fyid,substring(cast(bd.transactionid as varchar),4,1) as trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,cast(bd.voucherdate as date) as voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from "
								+ table
								+ " bd join bankaccount ba on ba.id=bd.bankorgid  where transactionid=? and bd.bankid=? and bd.paymentmethod=2";
						Map<String, Object> fdata = db.getSingleResultMap(sql,
								Arrays.asList(transactionid, auth.getBankId()));
						return Messenger.getMessenger().setData(fdata).success();
					}
					JSONObject d = dt.getJSONObject("data");
					DbResponse rs = db.execute("insert into " + table
							+ " (id,fyid,transactionid,officename,collectioncenterid,lgid,voucherdate,voucherdateint,bankid,accountnumber,amount,usestatus,bankorgid) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
							Arrays.asList(d.get("id"), d.get("fyid"), d.get("transactionid"), d.get("officename"),
									d.get("collectioncenterid"), d.get("lgid"), d.get("voucherdate"),
									d.get("voucherdateint"), d.get("bankid"), d.get("accountnumber"), d.get("amount"),
									d.get("usestatus"),d.get("bankorgid")));
					if (rs.getErrorNumber() == 0) {
						sql = "select bd.usestatus,bd.fyid,substring(cast(bd.transactionid as varchar),4,1) as trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,cast(bd.voucherdate as date) as voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from "
								+ table
								+ " bd join bankaccount ba on ba.id=bd.bankorgid  where transactionid=? and bd.bankid=? and bd.paymentmethod=2";
						Map<String, Object> fdata = db.getSingleResultMap(sql,
								Arrays.asList(transactionid, auth.getBankId()));
						return Messenger.getMessenger().setData(fdata).success();
					} else {
						return Messenger.getMessenger().setMessage(rs.getMessage()).error();
					}
				} else {
					return Messenger.getMessenger().setMessage(dt.getString("message")).error();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		} else {
			return Messenger.getMessenger().setMessage("Cannot Connect to SuTRA Server.").error();
		}
		return Messenger.getMessenger().error();
	}
	
	public ResponseEntity<Map<String, Object>> getTransDetailsSutra() {
		String transactionid = request("transactionid");
		transactionid = nep2EngNum(transactionid).trim();
		if (!isKarobarsanketValid(transactionid)) {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		char forthChar = transactionid.charAt(3);
		if (forthChar == '2') {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		if (forthChar == '9') {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		if (forthChar == '4' || forthChar == '5') {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		
//		String sql = "select usestatus from " + table + " where transactionid=? and bankid=? and paymentmethod=2";
//		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid, auth.getBankId()));
//		if (data != null) {
//			sql = "select bd.usestatus,bd.fyid,substring(cast(bd.transactionid as varchar),4,1) as trantype,bd.taxpayername,bd.vatpno,bd.address,bd.transactionid,bd.officename,bd.collectioncenterid,bd.lgid,cast(bd.voucherdate as date) as voucherdate,bd.voucherdateint,bd.bankid,bd.accountnumber,bd.amount,ba.accountname from "
//					+ table
//					+ " bd join bankaccount ba on ba.id=bd.bankorgid  where transactionid=? and bd.bankid=? and bd.paymentmethod=2";
//			Map<String, Object> fdata = db.getSingleResultMap(sql,
//					Arrays.asList(transactionid, auth.getBankId()));
//			return Messenger.getMessenger().setData(fdata).success();
//		}
		JSONObject dt = api.getTransDetailsForview(transactionid);
		System.out.println(dt.toString());
		if (dt != null) {
			try {
				if (dt.getInt("status") == 1) {

					JSONObject d = dt.getJSONObject("data");
						return Messenger.getMessenger().setData(d.toString()).success();

				} else {
					return Messenger.getMessenger().setMessage(dt.getString("message")).error();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		} else {
			return Messenger.getMessenger().setMessage("Cannot Connect to SuTRA Server.").error();
		}
		return Messenger.getMessenger().error();
	}
	
	@Transactional
	private ResponseEntity<Map<String, Object>> getTransDetailsRmisPortal() {
		String transactionid = request("transactionid");
		transactionid = nep2EngNum(transactionid);
		if (!isKarobarsanketValid(transactionid)) {
			return Messenger.getMessenger().setMessage("Invalid Karobarsanket format.").error();
		}
		
		String sql = "select isused,approved from taxvouchers where karobarsanket=? and bankid=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid, auth.getBankId()));
		if (data != null) {
			if (!(data.get("isused") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("Transactionid already been used.").error();
			}
			String sqls = "select bd.isused as usestatus,bd.fyid,substring(cast(bd.karobarsanket as varchar),4,1) as trantype,bd.taxpayername,bd.taxpayerpan as vatpno,0 as address,bd.karobarsanket as transactionid,admin_local_level_structure.namenp as officename,bd.collectioncenterid,bd.lgid,cast(bd.date as date) as voucherdate,bd.dateint as voucherdateint,bd.bankid,bd.accountnumber,bd.amountcr as amount,ba.accountname from taxvouchers bd "							
					+ "  join bankaccount ba on ba.id=bd.bankorgid join admin_local_level_structure on admin_local_level_structure.id=bd.lgid where karobarsanket=? and bd.bankid=? ";
			Map<String, Object> fdata = db.getSingleResultMap(sqls,
					Arrays.asList(transactionid, auth.getBankId()));
			return Messenger.getMessenger().setData(fdata).success();
		}
		JSONObject dt = api.getTransDetails(transactionid);
		System.out.println(dt.toString());
		if (dt != null) {
			try {
				if (dt.getInt("status") == 1) {
					
					JSONObject d = dt.getJSONObject("data");
					;
					JSONArray dtl = dt.getJSONArray("detail");
					System.out.println(dtl);
					
					DbResponse rs = db.execute("insert into taxvouchers (id,fyid,karobarsanket,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountnumber,bankid,branchid,bankorgid,amountcr,depositbankid,depositbranchid,deposituserid,ttype) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
							Arrays.asList(d.get("id"), d.get("fyid"), d.get("transactionid"),d.get("voucherdate"), d.get("taxpayername"),d.get("taxpayerpan"),d.get("taxpayername"),d.get("depcontact"),d.get("lgid"),
									d.get("collectioncenterid"), d.get("accountnumber"), d.get("bankid"),auth.getBranchId(),d.get("bankorgid"),d.get("amount"),auth.getBankId(),auth.getBranchId(),auth.getUserId(),3
									));
					System.out.println("here i am "+ rs);
					if (dtl.length() > 0) {
						for (int i = 0; i < dtl.length(); i++) {
							JSONObject objects = dtl.getJSONObject(i);
							String sq1 = "INSERT INTO taxvouchers_detail (id,mainid,revenueid,amount) values(?,?,?)";
							db.execute(sq1, Arrays.asList(objects.get("did"), objects.get("mainid"), objects.get("revenueid"), objects.get("amount")));
						}
					}
					System.out.println("here");
					if (rs.getErrorNumber() == 0) {
						sql = "select bd.isused as usestatus,bd.fyid,substring(cast(bd.karobarsanket as varchar),4,1) as trantype,bd.taxpayername,bd.taxpayerpan as vatpno,0 as address,bd.karobarsanket as transactionid,admin_local_level_structure.namenp as officename,bd.collectioncenterid,bd.lgid,cast(bd.date as date) as voucherdate,bd.dateint as voucherdateint,bd.bankid,bd.accountnumber,bd.amountcr as amount,ba.accountname from taxvouchers bd "							
								+ "  join bankaccount ba on ba.id=bd.bankorgid join admin_local_level_structure on admin_local_level_structure.id=bd.lgid where karobarsanket=? and bd.bankid=? ";
						Map<String, Object> fdata = db.getSingleResultMap(sql,
								Arrays.asList(transactionid, auth.getBankId()));
						return Messenger.getMessenger().setData(fdata).success();
					} else {
						return Messenger.getMessenger().setMessage(rs.getMessage()).error();
					}
				} else {
					return Messenger.getMessenger().setMessage(dt.getString("message")).error();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			}
		} else {
			return Messenger.getMessenger().setMessage("Cannot Connect to SuTRA Server.").error();
		}
		return Messenger.getMessenger().error();
		
	}

	@Transactional
	private ResponseEntity<Map<String, Object>> getTransDetailsCheque() {
		String transactionid = request("transactionid");
		transactionid = nep2EngNum(transactionid);
		String sql = "select bd.fyid,substring(cast(bd.karobarsanketno as varchar),4,1) as trantype,bd.cdid,bd.karobarSanketNo,bd.orgid as lgid,bd.trandate,bd.trandatetint,bd.bankid,bd.accountno,ba.accountname,bi.namenp as bankname,ll.namenp as palika from chequeBankDakhilaMain bd join bankaccount ba on ba.id=bd.bankorgid join bankinfo bi on bi.id=bd.bankid join admin_local_level_structure ll on ll.id=bd.adminid where karobarSanketNo=? and bd.bankid=? ";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid, auth.getBankId()));
		if (data == null) {
			JSONObject dt = api.getChequeDetails(transactionid);
			if (dt != null) {
				try {
					if (dt.getInt("status") == 1) {
//						System.out.println("here");
						JSONObject d = dt.getJSONObject("data");
						DbResponse rf = db.execute(
								"insert into chequeBankDakhilaMain (cdid ,adminid ,orgid ,fyid ,trantype ,karobarSanketNo ,trandate ,trandatetint ,refNo ,narration ,bankorgid ,bankid ,accountno ,entrydate ,enterby) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
								Arrays.asList(d.get("cdid"), d.get("adminid"), d.get("orgid"), d.get("fyid"),
										d.get("trantype"), d.get("karobarsanketno"), d.get("trandate"),
										d.get("trandatetint"), d.get("refno"), d.get("narration"), d.get("bankorgid"),
										d.get("bankid"), d.get("accountno"), d.get("entrydate"), d.get("enterby")));

						JSONArray dtls = d.getJSONArray("details_rows");
						if (dtls.length() > 0) {
							for (int i = 0; i < dtls.length(); i++) {
								JSONObject dd = dtls.getJSONObject(i);
								db.execute(
										"insert into chequeBankDakhilaDetail(did ,mainid ,rcid ,ksno ,bankid ,chequeno ,chequeamount ,taxpayername ) values(?,?,?,?,?,?,?,?)",
										Arrays.asList(dd.get("did"), dd.get("mainid"), dd.get("rcid"), dd.get("ksno"),
												dd.get("bankid"), dd.get("chequeno"), dd.get("chequeamount"),
												dd.get("taxpayername")));

							}
						}
						sql = "select bd.fyid,substring(cast(bd.karobarsanketno as varchar),4,1) as trantype,bd.cdid,bd.karobarSanketNo,bd.orgid as lgid,bd.trandate,bd.trandatetint,bd.bankid,bd.accountno,ba.accountname,bi.namenp as bankname,ll.namenp as palika from chequeBankDakhilaMain bd join bankaccount ba on ba.id=bd.bankorgid join bankinfo bi on bi.id=bd.bankid join admin_local_level_structure ll on ll.id=bd.adminid where karobarSanketNo=? and bd.bankid=?";
						Map<String, Object> fdata = db.getSingleResultMap(sql,
								Arrays.asList(transactionid, auth.getBankId()));
						String sqld = "select cast(cb.did as varchar) as did ,cb.mainid ,cb.rcid ,cb.ksno ,cb.bankid ,cb.chequeno ,cb.chequeamount ,cb.taxpayername ,cb.isbankreceived ,cb.bankreceivedby ,cb.bankreceiveddate,bi.namenp as bankname from chequeBankDakhilaDetail cb join bankinfo bi on bi.id=cb.bankid where mainid=?";
						List<Map<String, Object>> dtl = db.getResultListMap(sqld, Arrays.asList(fdata.get("cdid")));
						fdata.put("details", dtl);
						return Messenger.getMessenger().setData(fdata).success();
					} else {
						return Messenger.getMessenger().setMessage(dt.getString("message")).error();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				return Messenger.getMessenger().setMessage("Cannot Connect to SuTRA Server.").error();
			}
			return Messenger.getMessenger().setMessage("No such transaction found.").error();
		}
		String sqld = "select cast(cb.did as varchar) as did ,cb.mainid ,cb.rcid ,cb.ksno ,cb.bankid ,cb.chequeno ,cb.chequeamount ,cb.taxpayername ,cb.isbankreceived ,cb.bankreceivedby ,cb.bankreceiveddate,bi.namenp as bankname from chequeBankDakhilaDetail cb join bankinfo bi on bi.id=cb.bankid where mainid=? and isbankreceived=?";
		List<Map<String, Object>> dtl = db.getResultListMap(sqld, Arrays.asList(data.get("cdid"), 0));
		data.put("details", dtl);
		if (dtl.size() > 0) {
			return Messenger.getMessenger().setData(data).success();
		} else {
			return Messenger.getMessenger()
					.setMessage("Already Received Checque, Please check list of checque deposits.").error();
		}
	}

	/*
	 * To Be Called by SuTRA application, to get the deposit voucher details using
	 * the payment reference number
	 */
	public ResponseEntity<Map<String, Object>> getVoucherStatus() {
		String transactionid = request("transactionid");
		if (transactionid.isBlank()) {
			return Messenger.getMessenger().setMessage("Transaction id is required").error();
		}
		String sql = "select transactionid,bankvoucherno,depositdate,remarks,usestatus from " + table
				+ " where transactionid=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(transactionid));
		return Messenger.getMessenger().setData(data).success();
	}
}