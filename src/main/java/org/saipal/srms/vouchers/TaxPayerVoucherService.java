package org.saipal.srms.vouchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.service.IrdPanSearchService;
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
public class TaxPayerVoucherService extends AutoService {

	@Autowired
	Authenticated auth;

	@Autowired
	ApiManager api;

	@Autowired
	IrdPanSearchService pan;

	private String table = "taxvouchers";

	public ResponseEntity<Map<String, Object>> index() {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where tx.id!=1  and ttype=1 ";
		String approve = request("approve");
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = TaxPayerVoucher.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}

		if (!approve.isBlank()) {
			condition += " and tx.approved='" + approve + "'";
		}

		String sort = "";
		if (!request("sortKey").isBlank()) {
			if (!request("sortDir").isBlank()) {
				sort = request("sortKey") + " " + request("sortDir");
			}
		}
		if (sort.isBlank()) {
			sort = "date desc";
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("cast(tx.id as char) as id,cast(date as date) as date,tx.approved, voucherno,karobarsanket,taxpayername,taxpayerpan,depositedby,depcontact,tx.lgid,collectioncenterid,bankorgid,purpose,ba.accountnumber as accountno,amountcr as amount")
				.sqlBody("from taxvouchers tx join bankaccount ba on  ba.id = tx.bankorgid "+ condition).paginate();
//		System.out.println(result);
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> indexcheque() {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where tx.id!=1 and ttype=2  ";
		String approve = request("approvelog");
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
		if (sort.isBlank()) {
			sort = "date desc";
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("cast(tx.id as char) as id,cast(date as date) as date,cstatus,voucherno,taxpayername,karobarsanket,taxpayerpan,depositedby,depcontact,tx.lgid,collectioncenterid,bankorgid,purpose,chequeamount as amount, ba.accountnumber as accountno")
				.sqlBody("from " + table + " tx join bankaccount ba on ba.id=tx.bankorgid" +condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getSpecific(String id) {
		String sql = "select cast(bd.id as varchar) as id,karobarsanket, cast(bd.lgid as varchar) as lgid, cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.bankorgid,"
				+ "amountcr as amount, ba.accountnumber as accountno ,bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd "
				+ "join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+" join bankaccount ba on ba.id = bd.bankorgid "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid where bd.id=?";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(id));
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(id));
		data.put("revs", revs);
		return ResponseEntity.ok(data);
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> store() throws JSONException {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String voucher = request("voucherinfo");
		if (voucher.startsWith("{")) {
			voucher = "[" + voucher + "]";
		}
		JSONArray jarr = new JSONArray(voucher);
		String sql = "";
		TaxPayerVoucher model = new TaxPayerVoucher();
		model.loadData(document);
		if(isdayclosed(model.lgid,model.bankorgid)) {
			return Messenger.getMessenger().setMessage("Cannot update record.Already Day closed.").error();
		}
//		String usq = "select count(voucherno) from taxvouchers where voucherno=? and bankid=?";
//		Tuple res = db.getSingleResult(usq, Arrays.asList(model.voucherno, auth.getBankId()));
//		if ((!(res.get(0) + "").equals("0"))) {
//			return Messenger.getMessenger().setMessage("This voucherno is already in use.").error();
//		}
		if (model.taxpayerpan.isBlank()) {
			model.taxpayerpan = "0";
		}
		if (model.amount.isBlank()) {
			model.amount = "0";
		}
		if (model.chequebank.isBlank()) {
			model.chequebank = "0";
		}
		if (model.chequeamount.isBlank()) {
			model.chequeamount = "0";
		}
		
		String id = db.newIdInt();
		sql = "INSERT INTO taxvouchers (id,date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankorgid,purpose,deposituserid, bankid, branchid,ttype,chequebank,chequeno,chequeamount,chequetype,dateint,amountcr,depositbankid,depositbranchid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,format(getdate(),'yyyyMMdd'),?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(id, model.date, model.voucherno, model.taxpayername, model.taxpayerpan, model.depositedby,
						model.depcontact, model.lgid, model.collectioncenterid, model.bankorgid,
						model.purpose, auth.getUserId(), auth.getBankId(), auth.getBranchId(),
						model.ttype, model.chequebank, model.chequeno, model.chequeamount,model.chequetype,model.amount,auth.getBankId(),auth.getBranchId()));
		if (rowEffect.getErrorNumber() == 0) {
			if (jarr.length() > 0) {
				for (int i = 0; i < jarr.length(); i++) {
					JSONObject objects = jarr.getJSONObject(i);
					String sq1 = "INSERT INTO taxvouchers_detail (did,mainid,revenueid,amount) values(?,?,?,?)";
					db.execute(sq1, Arrays.asList(db.newIdInt(), id, objects.get("rc"), objects.get("amt")));
				}
			}
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
			if (autoVerify && (model.ttype.equals("1"))) {
				db.execute("update " + table + " set approved=1,updatedon=getdate(),approverid=? where id=?",
						Arrays.asList(auth.getUserId(), id));
				Tuple t = db.getSingleResult("select * from " + table + " where id=? and approved=1",
						Arrays.asList(id));
				if (t != null) {
					String revs = "";
					List<Tuple> list = db.getResultList(
							"select concat(did,'|',revenueid,'|',amount) as ar from taxvouchers_detail where mainid=?",
							Arrays.asList(id));
					if (list.size() > 0) {
						for (Tuple tp : list) {
							revs += tp.get(0) + ",";
						}
						revs = revs.substring(0, (revs.length() - 1));
					}
					try {
						JSONObject obj = api.sendDataToSutra(t, revs);
						if (obj != null) {
							if (obj.getInt("status") == 1) {
								db.execute("update taxvouchers set syncstatus=2 where id=?", Arrays.asList(id));
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			String usq = "select karobarsanket, approved from taxvouchers where id=?";
			Map <String, Object> res = db.getSingleResultMap(usq, Arrays.asList(id));
			
			return Messenger.getMessenger().setData(res).success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {
		String sql = "select date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,llgcode,llgname,costcentercode,costcentername,bankorgid,purpose,amountcr as amount from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		TaxPayerVoucher model = new TaxPayerVoucher();
		model.loadData(document);
		String sql = "UPDATE " + table
				+ " set date=?,voucherno=?,taxpayername=?,taxpayerpan=?,depositedby=?,depcontact=?,lgid=?,collectioncenterid=?,bankorgid=?,purpose=?,amountcr=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.date, model.voucherno, model.taxpayername, model.taxpayerpan, model.depositedby,
						model.depcontact, model.lgid, model.collectioncenterid, model.bankorgid,
						model.purpose, model.amount, id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> chequeclear() {
		String id = request("id");
		Tuple c = db.getSingleResult("select id,amount,cstatus from " + table + " where id=?", Arrays.asList(id));
		if ((c.get("cstatus") + "").equals("1")) {
			return Messenger.getMessenger().setMessage("Cheque is already Cleared.").error();
		}
		db.execute("update " + table + " set cstatus=1,updatedon=getdate(),approverid=? where id=?",
				Arrays.asList(auth.getUserId(), id));
		Tuple t = db.getSingleResult("select * from " + table + " where id=? and cstatus=1", Arrays.asList(id));
		if (t != null) {
			String revs = "";
			List<Tuple> list = db.getResultList(
					"select concat(did,'|',revenueid,'|',amount) as ar from taxvouchers_detail where mainid=?",
					Arrays.asList(id));
			if (list.size() > 0) {
				for (Tuple tp : list) {
					revs += tp.get(0) + ",";
				}
				revs = revs.substring(0, (revs.length() - 1));
			}
			try {
				JSONObject obj = api.sendDataToSutra(t, revs);
				if (obj != null) {
					if (obj.getInt("status") == 1) {
						db.execute("update taxvouchers set syncstatus=2 where id=?", Arrays.asList(id));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return Messenger.getMessenger().setMessage("Cheque cleared.").success();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sq="select (case when ttype=1 then approved else cstatus end) as status  from "+table+" where id=?";
		Tuple tt=db.getSingleResult(sq,Arrays.asList(id));
		if((tt.get(0)+"").equals("1")) {
			return Messenger.getMessenger().setMessage("This voucher can not be deleted.").error();
		}
		String sql = "delete from " + table + " where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> approveVoucher(String id) {
		Tuple c = db.getSingleResult("select id,amountcr as amount,approved from " + table + " where id=?", Arrays.asList(id));
		if ((c.get("approved") + "").equals("1")) {
			return Messenger.getMessenger().setMessage("Voucher is already Approved.").error();
		}
		Tuple u = db.getSingleResult("select id,amountlimit,permid from users where id=?",
				Arrays.asList(auth.getUserId()));
		if ((u.get("permid") + "").equals("3")) {
			if (!(u.get("amountlimit") + "").equals("-1")) {
				if (Float.parseFloat(c.get("amount") + "") > Float.parseFloat(u.get("amountlimit") + "")) {
					return Messenger.getMessenger()
							.setMessage("Amount Limit exceeds, Only upto Rs." + u.get("amountlimit") + " is allowed.")
							.error();
				}
			}
		}
		db.execute("update " + table + " set approved=1,updatedon=getdate(),approverid=? where id=?",
				Arrays.asList(auth.getUserId(), id));
		Tuple t = db.getSingleResult("select * from " + table + " where id=? and approved=1", Arrays.asList(id));
		if (t != null) {
			String revs = "";
			List<Tuple> list = db.getResultList(
					"select concat(did,'|',revenueid,'|',amount) as ar from taxvouchers_detail where mainid=?",
					Arrays.asList(id));
			if (list.size() > 0) {
				for (Tuple tp : list) {
					revs += tp.get(0) + ",";
				}
				revs = revs.substring(0, (revs.length() - 1));
			}
			try {
				JSONObject obj = api.sendDataToSutra(t, revs);
				if (obj != null) {
					if (obj.getInt("status") == 1) {
						db.execute("update taxvouchers set syncstatus=2 where id=?", Arrays.asList(id));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return Messenger.getMessenger().setMessage("Voucher approved.").success();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
	}
	
public ResponseEntity<Map<String,Object>> searchVoucher() {
		String voucherno = request("voucherno");
		if(voucherno.isBlank()) {
			return Messenger.getMessenger().setMessage("Voucherno is required").error();
		}
		String sql = "select cast(bd.id as varchar) as id,cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.bankorgid, "
				+ "amountcr as amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd "
				+ "join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid " 
				+ "where bd.karobarsanket=?";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(voucherno));
		if(data==null) {
			return Messenger.getMessenger().setMessage("No such transaction found.").error();
		}
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(data.get("id")));
		data.put("revs", revs);
		
		
		return Messenger.getMessenger().setData(data).success();
	}
	

	public ResponseEntity<List<Map<String, Object>>> getList() {
		String bankId = auth.getBankId();
		String sql = "";
		if (bankId.equals("1")) {
			sql = "select id,name from " + table + " where id !=1";
		} else {
			sql = "select id,name from " + table + " where id ='" + bankId + "'";
		}
		return ResponseEntity.ok(db.getResultListMap(sql));
	}

	public ResponseEntity<String> getBanksFromSutra() {
		return ResponseEntity.ok(api.getBanks().toString());
	}

	public ResponseEntity<String> getLocalLevels() {
		List<Tuple> d = db.getResultList(
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join branches ba on als.id=ba.dlgid and ba.bankid=? and ba.id=? order by als.namenp",
				Arrays.asList(auth.getBankId(), auth.getBranchId()));

		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("code", t.get("id") + "", "name", t.get("namenp")));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Local Level Not found\"}");
	}

	public ResponseEntity<String> getLocalLevelsAll() {
		String cond="";
		String sql="select top 1 dlgid from branches where bankid=? and id=?";
		Tuple tt=db.getSingleResult(sql,Arrays.asList(auth.getBankId(),auth.getBranchId()));
		if(tt.get(0)!=null) {
			cond= " where als.id <> "+tt.get(0);
		}
		List<Tuple> d = db.getResultList(
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? "+cond+" order by als.namenp",
				Arrays.asList(auth.getBankId()));

		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("code", t.get("id") + "", "name", t.get("namenp"), "id", t.get("id") + ""));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Local Level Not found\"}");
	}
	public ResponseEntity<String> getAllLocalLevels() {
		String cond="";
		List<Tuple> d = db.getResultList(
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? "+cond+" order by als.namenp",
				Arrays.asList(auth.getBankId()));

		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("code", t.get("id") + "", "name", t.get("namenp"), "id", t.get("id") + ""));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Local Level Not found\"}");
	}

	public ResponseEntity<String> getCostCentres() {
		String llgCode = request("llgcode");
		// check if data is cached
		List<Tuple> d = db.getResultList("select id,code,namenp,nameen from collectioncenter where lgid=?",
				Arrays.asList(llgCode));
		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("code", t.get("id") + "", "name", t.get("namenp")));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Cost Centres Not found\"}");
	}

	public ResponseEntity<String> getBankAccounts() {
		String llgCode = request("llgcode");
		if (llgCode.isBlank()) {
			return ResponseEntity.ok("{\"status\":0,\"message\":\"Local Level & Revenuecode is required\"}");
		}

		List<Tuple> d = db.getResultList(
				"select ba.accountname,ba.accountnumber,ba.id from bankaccount ba where ba.bankid=? and ba.lgid=? order by accounttype ",
				Arrays.asList(auth.getBankId(), llgCode));
		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("acno", t.get("accountnumber") + "", "name", t.get("accountname"), "id",
							t.get("id") + ""));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"No Bank A/C Found \"}");
	}

	public ResponseEntity<String> getRevenue() {
		String bankorgid = request("bankorgid");
		String b = db.getSingleResult("select accounttype from bankaccount where id=?", Arrays.asList(bankorgid)).get(0)
				+ "";
		String condi = " and code<=33300 ";
		if (Integer.parseInt(b) == 10) {
			condi = " and code > 33300 ";
		}
		// check if data is cached
		List<Tuple> d = db.getResultList("select code,namenp from crevenue where 1=1" + condi);
		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("code", t.get("code"), "name", t.get("namenp")));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok(api.revenueCodes().toString());
	}

	public ResponseEntity<String> getBank() {
//		System.out.println("getbvabnk");
		// check if data is cached
		List<Tuple> d = db.getResultList("select id,code,namenp from bankinfo where 1=1");
		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("id", t.get("id"), "name", t.get("namenp")));
				}
				j.put("status", 1);
				j.put("message", "Success");
				j.put("data", dt);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok(api.revenueCodes().toString());
	}

	public ResponseEntity<String> getAllDetails() {
		String llgCode = request("llgcode");
		if (llgCode.isBlank()) {
			return ResponseEntity.ok("{status:0,message:\"Local Level Code is required\"}");
		}
		try {
			JSONObject costcnt = new JSONObject(getCostCentres().getBody());
			JSONObject bankacs = new JSONObject(getBankAccounts().getBody());
			JSONObject j = new JSONObject();
			j.put("status", 1);
			j.put("message", "success");
			j.put("costcentres", costcnt.getJSONArray("data"));
			j.put("bankacs", bankacs.getJSONArray("data"));
			return ResponseEntity.ok(j.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Unable to fetch data.\"}");

	}

	/*
	 * To Be Called by SuTRA application, to get the Taxpayer voucher details if
	 * they are already not pushed to the Sutra
	 */
	public ResponseEntity<String> getVoucherDetailsByVoucherNo() {
		String karobarsanket = request("karobarsanket");
		String bankid = request("bankid");
		if (karobarsanket.isBlank()) {
			return ResponseEntity.ok("{status:0,message:\"Bank Voucher No. required\"}");
		}
		if (bankid.isBlank()) {
			return ResponseEntity.ok("{status:0,message:\"Bank is required\"}");
		}
		Tuple t = db.getSingleResult("select top 1 * from " + table + " where karobarsanket=? and bankid=? and approved=1",
				Arrays.asList(karobarsanket, bankid));
		if (t != null) {
			String revs = "";
			List<Tuple> list = db.getResultList(
					"select concat(did,'|',revenueid,'|',amount) as ar from taxvouchers_detail where mainid=?",
					Arrays.asList(t.get("id")));
			if (list.size() > 0) {
				for (Tuple tp : list) {
					revs += tp.get(0) + ",";
				}
				revs = revs.substring(0, (revs.length() - 1));
			}
			try {
				JSONObject data = new JSONObject();
				data.put("id", t.get("id") + "");
				data.put("date", t.get("date") + "");
				data.put("karobarsanket", t.get("karobarsanket") + "");
				data.put("voucherno", t.get("voucherno") + "");
				data.put("taxpayername", t.get("taxpayername") + "");
				data.put("taxpayerpan", t.get("taxpayerpan") + "");
				data.put("depositedby", t.get("depositedby") + "");
				data.put("depcontact", t.get("depcontact") + "");
				data.put("lgid", t.get("lgid") + "");
				data.put("collectioncenterid", t.get("collectioncenterid") + "");
				data.put("bankorgid", t.get("bankorgid") + "");
				data.put("purpose", t.get("purpose") + "");
				data.put("bankid", t.get("bankid") + "");
				data.put("branchid", t.get("branchid") + "");
				data.put("deposituserid", t.get("creatorid") + "");
				data.put("approved", t.get("approved") + "");
				data.put("approverid", t.get("approverid") + "");
				data.put("updatedon", t.get("updatedon") + "");
				data.put("chequebank", t.get("chequebank") + "");
				data.put("chequeamount", t.get("chequeamount") + "");
				data.put("chequeno", t.get("chequeno") + "");
				data.put("cstatus", t.get("cstatus") + "");
				data.put("ttype", t.get("ttype") + "");
				data.put("amountdr", t.get("amountdr") + "");
				data.put("amountcr", t.get("amountcr") + "");
				data.put("depositbankid", t.get("depositbankid") + "");
				data.put("depositbranchid", t.get("depositbranchid") + "");
				data.put("revenue", revs);
				JSONObject j = new JSONObject();
				j.put("status", 1);
				j.put("message", "success");
				j.put("data", data);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"No Such voucher exists.\"}");
	}

	public ResponseEntity<Map<String, Object>> getPanDetails() {
		String panno = request("panno");
		if (panno.isBlank()) {
			return Messenger.getMessenger().setMessage("Pan No. is Required").error();
		}
		try {
			JSONArray dt = pan.getData(panno);
			if (dt != null) {
				JSONObject jb = dt.getJSONObject(0);
				return Messenger.getMessenger().setData(Map.of("taxpayer", jb.getString("taxpayer"), "contactNo",
						jb.getString("contactNo") == null ? "" : jb.getString("contactNo"))).success();
			}
			return Messenger.getMessenger().setMessage("Invalid Pan No.").error();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return Messenger.getMessenger().setMessage("Invalid Pan No.").error();
		}
	}

	public ResponseEntity<Map<String, Object>> generateReport() {
		String voucher = request("voucherno");
		String palika = request("palika");
		String sql = "select  tv.approved,(case when tv.approved='1' then karobarsanket else 'To be Approved' end) as approved_text, dbo.eng2nep(dbo.getfiscalyear(date)) as fy,dbo.getrs(cast(tv.amountcr as float)) as amountwords,lls.namenp as llgname, bi.namenp, ba.accountname,karobarsanket as voucherno,karobarsanket,taxpayername, dbo.eng2nep(amountcr) as amount,dbo.eng2nep(ba.accountnumber) as accountno,dbo.eng2nep(depcontact) as depcontact ,dbo.eng2nep(taxpayerpan) as taxpayerpan, dbo.eng2nep(dbo.getnepdate(cast(date as date))) as date from "
				+ "taxvouchers tv " 
				+ "left join bankaccount ba on ba.id=tv.bankorgid "
				+ "left join bankinfo bi on bi.id=tv.bankid "
				+ "left join admin_local_level_structure lls on lls.id=tv.lgid "
				+ "where karobarsanket=? and tv.lgid=? ";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(voucher, palika));
		System.out.println("hello");
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> getRevenueDetails() {
		String voucher = request("voucherno");
		String palika = request("palika");
		String sql = "SELECT cr.namenp as revenuetitle, "
				+ "       dbo.eng2nep(ROW_NUMBER() OVER (ORDER BY tvd.revenueid)) as sn,  "
				+ "       dbo.getrs(cast(tvd.amount as float)) as amountwords,  "
				+ "       dbo.eng2nep(tvd.amount) as amount,  "
				+ "       dbo.eng2nep(tvd.revenueid) as revenuecode, "
				+ "       total_amount.amountwords as total_amount, "
				+ "       total_amount_no.totalamountno as total_amount_no " 
				+ "FROM taxvouchers_detail tvd  "
				+ "JOIN taxvouchers tv ON tv.id = tvd.mainid  " 
				+ "JOIN crevenue cr ON cr.code = tvd.revenueid  "
				+ "CROSS APPLY ( " + "    SELECT dbo.getrs(cast(sum(amount) as float)) as amountwords "
				+ "    FROM taxvouchers_detail " + "    WHERE mainid = tv.id " + ") as total_amount "
				+ "CROSS APPLY ( " + "    SELECT dbo.eng2nep(cast(sum(amount) as float)) as totalamountno "
				+ "    FROM taxvouchers_detail " + "    WHERE mainid = tv.id " + ") as total_amount_no "
				+ "WHERE tv.karobarsanket = ? AND tv.lgid = ?";

		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(voucher, palika));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
//				System.out.println(t.toString());
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("sn", t.get("sn"));
				mapadmlvl.put("revenuetitle", t.get("revenuetitle"));
				mapadmlvl.put("amountwords", t.get("amountwords"));
				mapadmlvl.put("amount", t.get("amount"));
				mapadmlvl.put("revenuecode", t.get("revenuecode"));
				mapadmlvl.put("total_amount", t.get("total_amount"));
				mapadmlvl.put("total_amount_no", t.get("total_amount_no"));
				list.add(mapadmlvl);
			}
			System.out.println("aaaa");
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
//		System.out.println(sql);
//		List<Tuple> data = db.getResultList(sql, Arrays.asList(voucher, palika));
//		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> getEditDetails() {
		String voucherno = request("voucherno");
		String sql = "select cast((format(getdate(),'yyyyMMdd')) as numeric) as today,bd.approved,bd.isused,bd.dateint,cast(bd.collectioncenterid as varchar) as collectioncenterid,cast(bd.lgid as varchar) as lgid,cast(bd.id as varchar) as id,bd.amountcr as amount,cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, ba.accountnumber as accountno " 
				+ "bd.bankorgid,"
				+ " bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd  "
				+ "join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+"join bankaccount ba on ba.id = bd.bankorgid "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid " 
				+ "where bd.karobarsanket=?";
		Map<String, Object> t = db.getSingleResultMap(sql,Arrays.asList(voucherno));
		if(t==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
		}
		if((t.get("isused")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Already used voucher").error();
		}
		
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid as rc,concat(td.revenueid,'[',cr.namenp,']') as rv,td.amount as amt from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(t.get("id")+""));
		t.put("revs", revs);
		if((t.get("approved")+"").equals("0")) {
			return Messenger.getMessenger().setData(t).success();
		}
		JSONObject sdata = api.getVoucherDetails(t.get("id")+"");
//		System.out.println(sdata);
		if(sdata!=null) {
			try {
				if(sdata.getInt("status")==1) {
					JSONObject d = sdata.getJSONObject("data");
					if(d.getInt("isused")==1) {
						db.execute("update "+table+" set isused=1 where id=?",Arrays.asList(t.get("id")));
						return Messenger.getMessenger().setMessage("Already used voucher").error();
					}else {
						return Messenger.getMessenger().setData(t).success();
					}
				}
			}catch (JSONException e) {
				// TODO: handle exception
			}
		}
		return Messenger.getMessenger().setMessage("Cannot determine the status, Please try again later.").error();
	}
	
	@Transactional
	public ResponseEntity<Map<String, Object>> saveEditDetails() throws JSONException {
		String id = request("id");
		String taxpayername = request("taxpayername");
		String taxpayerpan = request("taxpayerpan").isBlank()?"0":request("taxpayerpan");
		String amount = request("amount");
		String voucher = request("voucherinfo");
		String lgid = request("lgid");
		String ccid= request("collectioncenterid");
		String acno = request("bankorgid");
		if (voucher.startsWith("{")) {
			voucher = "[" + voucher + "]";
		}
		if(isdayclosed(lgid,acno)) {
			return Messenger.getMessenger().setMessage("Cannot update record.Already Day closed.").error();
		}
		JSONArray jarr = new JSONArray(voucher);
		String sql = "select  top 1 *,cast((format(getdate(),'yyyyMMdd')) as numeric) as today from "+table+" where id=? and bankid=? and branchid=?";
		Map<String,Object> t = db.getSingleResultMap(sql,Arrays.asList(id,auth.getBankId(),auth.getBranchId()));
		if(t==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
		}
		if((t.get("isused")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Already used voucher").error();
		}
		if((t.get("approved")+"").equals("0")) {
			if((t.get("today")+"").equals(t.get("dateint")+"")) {
				if(!(t.get("lgid")+"").equals(lgid)) {
					//same day palika change
					db.execute("insert into taxvouchers_log (id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid) select id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid from "+table+" where id=?",Arrays.asList(id));
					db.execute("insert into taxvouchers_log (id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid) select id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountcr ,amountdr ,depositbankid ,depositbranchid ,deposituserid from "+table+" where id=?",Arrays.asList(id));
					db.execute("update "+table+" set amountcr=?,lgid=?,collectioncenterid=?,bankorgid=? where id=?",Arrays.asList(amount,lgid,ccid,acno,id));
					
					db.execute("update "+table+" set taxpayername=? ,taxpayerpan=?,amountcr=? where id=?",Arrays.asList(taxpayername,taxpayerpan,amount,id));
					db.execute("delete from taxvouchers_detail where mainid=?",Arrays.asList(id));
					if (jarr.length() > 0) {
						for (int i = 0; i < jarr.length(); i++) {
							JSONObject objects = jarr.getJSONObject(i);
							String sq1 = "INSERT INTO taxvouchers_detail (did,mainid,revenueid,amount) values(?,?,?,?)";
							db.execute(sq1, Arrays.asList(db.newIdInt(), id, objects.get("rc"), objects.get("amt")));
						}
					}
					return Messenger.getMessenger().setMessage("Voucher Updated").success();
				}else {
					return Messenger.getMessenger().setMessage("Cannot transfer to same local level.").success();
				}
			}else {
				db.execute("update "+table+" set taxpayername=? ,taxpayerpan=? where id=?",Arrays.asList(taxpayername,taxpayerpan,id));
				return Messenger.getMessenger().setMessage("Voucher Updated").success();
				
			}
//			return Messenger.getMessenger().setMessage("Voucher Updated").success();
		}
		JSONObject sdata = api.getVoucherDetails(t.get("id")+"");
		if(sdata!=null) {
			try {
				if(sdata.getInt("status")==1) {
					JSONObject d = sdata.getJSONObject("data");
					if(d.getInt("isused")==1) {
						db.execute("update "+table+" set isused=1 where id=?",Arrays.asList(t.get("id")));
						return Messenger.getMessenger().setMessage("Already used voucher").error();
					}else {
						if((t.get("today")+"").equals(t.get("dateint")+"")) {
							if(!(t.get("lgid")+"").equals(lgid)) {
								//same day palika change
								db.execute("insert into taxvouchers_log (id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid) select id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid from "+table+" where id=?",Arrays.asList(id));
								db.execute("insert into taxvouchers_log (id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid) select id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountcr ,amountdr ,depositbankid ,depositbranchid ,deposituserid from "+table+" where id=?",Arrays.asList(id));
								db.execute("update "+table+" set amountcr=?,lgid=?,collectioncenterid=?,bankorgid=? where id=?",Arrays.asList(amount,lgid,ccid,acno,id));
							}
//							System.out.println("here i am ");
							JSONObject ups = api.saveVoucherUpdates(id,taxpayername,taxpayerpan,amount,lgid,ccid,acno,voucher);
							if(ups!=null) {
								if(ups.getInt("status")==1) {
									db.execute("update "+table+" set taxpayername=? ,taxpayerpan=?,amountcr=? where id=?",Arrays.asList(taxpayername,taxpayerpan,amount,id));
									db.execute("delete from taxvouchers_detail where mainid=?",Arrays.asList(id));
									if (jarr.length() > 0) {
										for (int i = 0; i < jarr.length(); i++) {
											JSONObject objects = jarr.getJSONObject(i);
											String sq1 = "INSERT INTO taxvouchers_detail (did,mainid,revenueid,amount) values(?,?,?,?)";
											db.execute(sq1, Arrays.asList(db.newIdInt(), id, objects.get("rc"), objects.get("amt")));
										}
									}
									return Messenger.getMessenger().setMessage("Voucher Updated").success();
								}else {
									return Messenger.getMessenger().setMessage("Unable to update, Try again later").error();
								}
							}
							return Messenger.getMessenger().setMessage("Unable to update, Try again later").error();
						}else {
							JSONObject ups =  api.saveVoucherUpdates(id,taxpayername,taxpayerpan,"","","","","");
							if(ups!=null) {
								if(ups.getInt("status")==1) {
									db.execute("update "+table+" set taxpayername=? ,taxpayerpan=? where id=?",Arrays.asList(taxpayername,taxpayerpan,id));
									return Messenger.getMessenger().setMessage("Voucher Updated").success();
								}else {
									return Messenger.getMessenger().setMessage("Unable to update, Try again later").error();
								}
							}
							return Messenger.getMessenger().setMessage("Unable to update, Try again later").error();
						}
					}
				}
			}catch (JSONException e) {
				// TODO: handle exception
			}
			
		}		
		return Messenger.getMessenger().setMessage("Cannot determine the status, Please try again later.").error();
	}
	
	public ResponseEntity<Map<String, Object>> getEditDetailsOff() {
		String voucherno = request("voucherno");
		String sql = "select cast((format(getdate(),'yyyyMMdd')) as numeric) as today,bd.approved,cast(bankaccount.accountname as varchar) as accountname,bd.hasChangeReqest,bd.dateint,cast(bd.lgid as varchar) as lgid,cast(bd.id as varchar) as id,bd.amountcr as amount,cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.bankorgid, "
				+ " bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd  join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ " join bankaccount on bankaccount.id=bd.bankorgid join admin_local_level_structure lls on lls.id = bd.lgid " + "where bd.karobarsanket=? ";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(voucherno));
		if(data==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
		}
		if((data.get("today")+"").equals((data.get("dateint")+""))) {
			return Messenger.getMessenger().setMessage("Not able to update this voucher, Please use Same day Voucher Modification.").error();
		}
		if((data.get("isused")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Already used voucher").error();
		}
		if((data.get("hasChangeReqest")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Change request is already in process.").error();
		}
		
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(data.get("id")+""));
		data.put("revs", revs);
		JSONObject sdata = api.getVoucherDetails(data.get("id")+"");
		if(sdata!=null) {
			try {
				//data not found in sutra
				if(sdata.getInt("status")==2) {
					return Messenger.getMessenger().setData(data).success();
				}
				if(sdata.getInt("status")==1) {
					JSONObject d = sdata.getJSONObject("data");
					if(d.getInt("isused")==1) {
						db.execute("update "+table+" set isused=1 where id=?",Arrays.asList(data.get("id")));
						return Messenger.getMessenger().setMessage("Already used voucher").error();
					}else {
						return Messenger.getMessenger().setData(data).success();
					}
				}
			}catch (JSONException e) {
				// TODO: handle exception
			}
			
		}
		return Messenger.getMessenger().setMessage("Cannot determine the status, Please try again later.").error();
	}
	
	@Transactional
	public ResponseEntity<Map<String, Object>> saveEditDetailsOff() {
		String id = request("id");
		String lgid=request("lgid");
		String ccid=request("collectioncenterid");
		String acno=request("bankorgid");
		String remarks=request("remarks");
		if(id.isBlank() ||lgid.isBlank() || ccid.isBlank() || acno.isBlank()|| remarks.isBlank()) {
			return Messenger.getMessenger().setMessage("Required fields are not supplied.").error();
		}
		
		String sql = "select  top 1 *,cast((format(getdate(),'yyyyMMdd')) as numeric) as today from "+table+" where id=? and bankid=? and branchid=?";
		Map<String,Object> t = db.getSingleResultMap(sql,Arrays.asList(id,auth.getBankId(),auth.getBranchId()));
		if(t==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
		}
		if((t.get("today")+"").equals((t.get("dateint")+""))) {
			return Messenger.getMessenger().setMessage("Not able to update this voucher, Please use Same day Voucher Modification.").error();
		}
		
		if((t.get("isused")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Already used voucher").error();
		}
		if((t.get("lgid")+"").equals(lgid)) {
			return Messenger.getMessenger().setMessage("Canot transfer to same local level.").error();
		}
		if((t.get("hasChangeReqest")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Change request is already in process.").error();
		}
		
		JSONObject sdata = api.getVoucherDetails(t.get("id")+"");
		if(sdata!=null) {
			try {
				if(sdata.getInt("status")==1) {
					JSONObject d = sdata.getJSONObject("data");
					if(d.getInt("isused")==1) {
						db.execute("update "+table+" set isused=1 where id=?",Arrays.asList(t.get("id")));
						return Messenger.getMessenger().setMessage("Already used voucher").error();
					}
				}
				String llid = db.newIdInt();
				db.execute("update "+table+" set hasChangeReqest=1 where id=?",Arrays.asList(id));
				db.execute("insert into taxvoucher_ll_change(id,vrefid,lgid,collectioncenterid,bankorgid,remarks,creatorid,palikaresponse) values (?,?,?,?,?,?,?,2)",
						Arrays.asList(llid,id,lgid,ccid,acno,remarks,auth.getUserId()));
				return Messenger.getMessenger().setMessage("Voucher change requst sent.").success();
			}catch (JSONException e) {
				// TODO: handle exception
			}
		}
		return Messenger.getMessenger().setMessage("Cannot determine the status, Please try again later.").error();
	}
	
	public ResponseEntity<Map<String, Object>> getVoucherTransfer() {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where ttype=1 and hasChangeReqest=1";
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
		if (sort.isBlank()) {
			sort = "date desc";
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("cast(bd.id as varchar) as id, cast(bd.lgid as varchar) as lgid, cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.bankorgid, "
				+ "bd.amountcr as amount, bd.purpose, ba.accountnumber as accountno ,bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby ")
				.sqlBody(" from taxvouchers as bd join collectioncenter cc on cc.id = bd.collectioncenterid join admin_local_level_structure lls on lls.id = bd.lgid join bankaccount ba on ba.id=bd.bankorgid " + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	

	public ResponseEntity<List<Map<String, Object>>> getReport() {
		String startDate = request("from");
		String endDate = request("to");
		String sql = "SELECT * FROM "+ table +" WHERE Date >= '"+startDate+"' AND Date <= '"+endDate+"'";
		System.out.println(sql);
		
//		Map<String, Object> data = db.getSingleResultMap(sql);
		List<Map<String, Object>> data = db.getResultListMap(sql,Arrays.asList());
		return ResponseEntity.ok(data);
	}
	
	
	public ResponseEntity<Map<String, Object>> getSpecificAnotherPalika(String id) {
		String sql = "select cast(bd.id as varchar) as id, cast(bd.lgid as varchar) as lgid, cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.bankorgid, "
				+ "bd.amountcr as amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby, "
				+" als.namenp as tlnamenp,tcc.namenp as tcnamenp,ba.accountnumber,ba.accountname,bat.accountnumber as taccountnumber,bat.accountname as taccountname"
				+ " from taxvouchers as bd  "
				+ "join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid "
				+ "join taxvoucher_ll_change llc on llc.vrefid = bd.id and caseterminated = 0 "
				+ "join admin_local_level_structure als on als.id = llc.lgid "
				+ "join collectioncenter tcc on tcc.id = llc.collectioncenterid "
				+ "join bankaccount bat on bat.id = llc.bankorgid "
				+ "join bankaccount ba on ba.id = bd.bankorgid "
				+ "where bd.id=? and hasChangeReqest=1";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(id));
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(id));
		data.put("revs", revs);
		Tuple t = db.getSingleResult("select top 1 * from taxvoucher_ll_change where vrefid=? and caseterminated=0",Arrays.asList(id));
		Map<String,Object> msg = new HashMap<>();
		if(t!=null) {
			if((t.get("palikaresponse")+"").equals("2")) {
				msg.put("palikaresponse", t.get("palikaresponse"));
				msg.put("responsereason", t.get("responsereason"));
				//JSONObject presp = api.getPalikaResponse(id,t.get("id")+"");
//				System.out.println(presp.toString());
//				if(presp!=null) {
//					try {
//						if(presp.getInt("status")==1) {
//							JSONObject sdata = presp.getJSONObject("data");
//							int repStatus = sdata.getInt("palikaresponse");
//							String reason = sdata.getString("responsereason");
//							String llcid = sdata.getString("id");
//							if(repStatus==0) {
//								msg.put("palikaresponse", "0");
//								msg.put("responsereason", "");
//							}else {
//								db.execute("update taxvoucher_ll_change set palikaresponse=? ,responsereason=? where id=?",Arrays.asList(repStatus,reason,llcid));
//								msg.put("palikaresponse", repStatus);
//								msg.put("responsereason", reason);
//							}
//						}
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
			}else {
				msg.put("palikaresponse", t.get("palikaresponse"));
				msg.put("responsereason", t.get("responsereason"));
			}
		}
		data.put("status", msg);
		return ResponseEntity.ok(data);
	}
	
	public ResponseEntity<Map<String, Object>> settlePalikaChange() {
		String id = request("id");
		String type=request("type");
		if(type.isBlank() || id.isBlank()) {
			return Messenger.getMessenger().setMessage("Invalid Request..").error();
		}
		if(!(type.equals("1") || type.equals("0"))) {
			return Messenger.getMessenger().setMessage("Invalid Operation").error();
		}
		Tuple t = db.getSingleResult("select top 1 * from taxvoucher_ll_change where vrefid=? and caseterminated=0",Arrays.asList(id));
		if(t!=null) {
			if(!(t.get("palikaresponse")+"").equals("2")) {
				return Messenger.getMessenger().setMessage("Voucher Cannot be updated, has invalid status.").success();
			}
			if(type.equals("1")) {
				try {
					JSONObject presp = api.settlePalikaChange(id,t.get("lgid")+"",t.get("collectioncenterid")+"",t.get("bankorgid")+"");
					if(presp!=null) {
						if(presp.getInt("status")==1) {
							db.execute("insert into taxvouchers_log (id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountdr ,amountcr ,depositbankid ,depositbranchid ,deposituserid) select id ,fyid ,voucherno ,karobarsanket ,date ,taxpayername ,taxpayerpan ,depositedby ,depcontact ,lgid ,collectioncenterid ,bankid ,branchid ,bankorgid ,purpose ,syncstatus ,approved ,approverid ,createdon ,updatedon ,tasklog ,approvelog ,ttype ,chequebank ,chequeno ,chequeamount ,cstatus ,chequetype ,dateint ,isused ,hasChangeReqest ,changeReqestDate ,amountcr ,amountdr ,depositbankid ,depositbranchid ,deposituserid from "+table+" where id=?",Arrays.asList(id));
							db.execute("update "+table+" set hasChangeReqest=0,lgid=?,collectioncenterid=?,bankorgid=? where id=?",Arrays.asList(t.get("lgid"),t.get("collectioncenterid"),t.get("bankorgid"),id));
							db.execute("update taxvoucher_ll_change set caseterminated=1 ,terminationdate=getDate() where vrefid=? and caseterminated=0",Arrays.asList(id));
							return Messenger.getMessenger().setMessage("Voucher Updated Successfully ").success();
						}
					}
				}catch(JSONException e) {
					
				}
				
			}else {
				db.execute("update "+table+" set hasChangeReqest=0 where id=?",Arrays.asList(id));
				db.execute("update taxvoucher_ll_change set caseterminated=1 ,terminationdate=getDate() where vrefid=? and caseterminated=0",Arrays.asList(id));
				return Messenger.getMessenger().setMessage("Voucher Update Cancelled").success();
			}
		}
		return Messenger.getMessenger().setMessage("Invalid Request..").error();
	}

	public ResponseEntity<String> getVoucherDetailsByVoucherId() {
		String id = request("id");
		if (id.isBlank()) {
			return ResponseEntity.ok("{status:0,message:\"Bank Voucher No. required\"}");
		}
		Tuple t = db.getSingleResult("select top 1 * from " + table + " where id=? and approved=1",
				Arrays.asList(id));
		if (t != null) {
			String revs = "";
			List<Tuple> list = db.getResultList(
					"select concat(did,'|',revenueid,'|',amount) as ar from taxvouchers_detail where mainid=?",
					Arrays.asList(t.get("id")));
			if (list.size() > 0) {
				for (Tuple tp : list) {
					revs += tp.get(0) + ",";
				}
				revs = revs.substring(0, (revs.length() - 1));
			}
			try {
				JSONObject data = new JSONObject();
				data.put("id", t.get("id") + "");
				data.put("date", t.get("date") + "");
				data.put("karobarsanket", t.get("karobarsanket") + "");
				data.put("voucherno", t.get("voucherno") + "");
				data.put("taxpayername", t.get("taxpayername") + "");
				data.put("taxpayerpan", t.get("taxpayerpan") + "");
				data.put("depositedby", t.get("depositedby") + "");
				data.put("depcontact", t.get("depcontact") + "");
				data.put("lgid", t.get("lgid") + "");
				data.put("collectioncenterid", t.get("collectioncenterid") + "");
				data.put("bankorgid", t.get("bankorgid") + "");
				data.put("purpose", t.get("purpose") + "");
				data.put("bankid", t.get("bankid") + "");
				data.put("branchid", t.get("branchid") + "");
				data.put("deposituserid", t.get("deposituserid") + "");
				data.put("approved", t.get("approved") + "");
				data.put("approverid", t.get("approverid") + "");
				data.put("updatedon", t.get("updatedon") + "");
				data.put("chequebank", t.get("chequebank") + "");
				data.put("chequeamount", t.get("chequeamount") + "");
				data.put("chequeno", t.get("chequeno") + "");
				data.put("cstatus", t.get("cstatus") + "");
				data.put("ttype", t.get("ttype") + "");
				data.put("amountdr", t.get("amountdr") + "");
				data.put("amountcr", t.get("amountcr") + "");
				data.put("depositbankid", t.get("depositbankid") + "");
				data.put("depositbranchid", t.get("depositbranchid") + "");
				data.put("revenue", revs);
				JSONObject j = new JSONObject();
				j.put("status", 1);
				j.put("message", "success");
				j.put("data", data);
				return ResponseEntity.ok(j.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"No Such voucher exists.\"}");
	}
}