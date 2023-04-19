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
		String condition = " where id!=1  and ttype=1 ";
		String approve = request("approve");
		System.out.println("The approval Id is:" + approve);
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
			condition += " and approved='" + approve + "'";
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
				.select("cast(id as char) as id,cast(date as date) as date,approved, voucherno,karobarsanket,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,amount")
				.sqlBody("from " + table + condition).paginate();
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
		String condition = " where id!=1 and ttype=2  ";
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
				.select("cast(id as char) as id,cast(date as date) as date,cstatus,voucherno,taxpayername,karobarsanket,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,chequeamount as amount")
				.sqlBody("from " + table + condition).paginate();
//				.select("t1.id,cast(date as date) as date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,SUM(t2.amount) as amount")
//				.sqlBody("from " + table +" t1 JOIN taxvouchers_detail t2 ON t1.id = t2.mainid"+ condition+" group by t1.id,date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose").paginate();
//		System.out.println(result);
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getSpecific(String id) {
		// String transactionid = request("id");
//		String sql = "select bd.id, bd.depositdate, bd.bankvoucherno, lls.namenp as llsname,cc.namenp as collectioncentername, bd.accountnumber, bd.amount, bd.remarks, bd.transactionid from bank_deposits as bd join collectioncenter cc on cc.id = bd.collectioncenterid join admin_local_level_structure lls on lls.id = bd.lgid where bd.id =" + id;
//<<<<<<< HEAD
//		String sql = "select cast(bd.id as varchar) as id,karobarsanket, cast(bd.lgid as varchar) as lgid, cast (bd.date as date) as date, bd.voucherno,\r\n"
//				+ "lls.namenp as llsname,cc.namenp as collectioncentername,\r\n" + "bd.accountno, bd.revenuetitle,\r\n"
//				+ "SUM(t2.amount) as amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby\r\n"
//				+ "from taxvouchers as bd left JOIN taxvouchers_detail t2 ON bd.id = t2.mainid join collectioncenter cc on cc.id = bd.collectioncenterid \r\n"
//				+ "join admin_local_level_structure lls on lls.id = bd.lgid\r\n" + "where bd.id=" + id
//				+ " group by bd.id,bd.date,bd.voucherno,bd.lgid,karobarsanket,lls.namenp,cc.namenp,bd.accountno,bd.revenuetitle, bd.purpose,bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby";
//=======
		String sql = "select cast(bd.id as varchar) as id,karobarsanket, cast(bd.lgid as varchar) as lgid, cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.accountno, bd.revenuetitle, "
				+ "SUM(t2.amount) as amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd left JOIN taxvouchers_detail t2 ON bd.id = t2.mainid join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid " + "where bd.id=" + id
				+ " group by bd.id,bd.date,bd.voucherno,karobarsanket,bd.lgid,lls.namenp,cc.namenp,bd.accountno,bd.revenuetitle, bd.purpose,bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby";

//		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql);
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
//		String usq = "select count(voucherno) from taxvouchers where voucherno=? and bankid=?";
//		Tuple res = db.getSingleResult(usq, Arrays.asList(model.voucherno, auth.getBankId()));
//		if ((!(res.get(0) + "").equals("0"))) {
//			return Messenger.getMessenger().setMessage("This voucherno is already in use.").error();
//		}
		if (model.taxpayerpan.isBlank()) {
			model.taxpayerpan = "0";
		}
		if (model.revenuecode.isBlank()) {
			model.revenuecode = "0";
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
		sql = "INSERT INTO taxvouchers (id,date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,amount,creatorid, bankid, branchid,ttype,chequebank,chequeno,chequeamount,chequetype,dateint) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,format(getdate(),'yyyyMMdd'))";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(id, model.date, model.voucherno, model.taxpayername, model.taxpayerpan, model.depositedby,
						model.depcontact, model.lgid, model.collectioncenterid, model.accountno, model.revenuecode,
						model.purpose, model.amount, auth.getUserId(), auth.getBankId(), auth.getBranchId(),
						model.ttype, model.chequebank, model.chequeno, model.chequeamount,model.chequetype));
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
			String usq = "select karobarsanket from taxvouchers where id=?";
			Tuple res = db.getSingleResult(usq, Arrays.asList(id));
			
			return Messenger.getMessenger().setData(res.get(0)+"").success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {
		String sql = "select date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,llgcode,llgname,costcentercode,costcentername,accountno,revenuecode,purpose,amount from "
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
				+ " set date=?,voucherno=?,taxpayername=?,taxpayerpan=?,depositedby=?,depcontact=?,lgid=?,collectioncenterid=?,accountno=?,revenuecode=?,purpose=?,amount=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.date, model.voucherno, model.taxpayername, model.taxpayerpan, model.depositedby,
						model.depcontact, model.lgid, model.collectioncenterid, model.accountno, model.revenuecode,
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
//		Tuple u = db.getSingleResult("select id,amountlimit,permid from users where id=?",Arrays.asList(auth.getUserId()));
//		if((u.get("permid")+"").equals("3")) {
//			if(!(u.get("amountlimit")+"").equals("-1")) {
//				if(Float.parseFloat(c.get("amount")+"")>Float.parseFloat(u.get("amountlimit")+"")) {
//					return Messenger.getMessenger().setMessage("Amount Limit exceeds, Only upto Rs."+u.get("amountlimit")+" is allowed.").error();
//				}
//			}
//		}
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
		Tuple c = db.getSingleResult("select id,amount,approved from " + table + " where id=?", Arrays.asList(id));
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
		System.out.println(voucherno);
		if(voucherno.isBlank()) {
			return Messenger.getMessenger().setMessage("Voucherno is required").error();
		}
		String sql = "select cast(bd.id as varchar) as id,cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.accountno, bd.revenuetitle, "
				+ "SUM(t2.amount) as amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd left JOIN taxvouchers_detail t2 ON bd.id = t2.mainid join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid " + "where bd.voucherno=" + voucherno
				+ " group by bd.id,bd.date,bd.voucherno,lls.namenp,cc.namenp,bd.accountno,bd.revenuetitle, bd.purpose,bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby";
//		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql);
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

//	public ResponseEntity<String> getVoucherDetails() {
//		String voucherno = request("voucherno");
//		if(voucherno.isBlank()) {
//			return ResponseEntity.ok("{\"status\":0,\"message\":\"Local Level Code is required\"}");
//		}
//		return ResponseEntity.ok(api.getVoucherDetails(voucherno).toString());
//	}

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
		String voucherno = request("voucherno");
		String bankid = request("bankid");
		if (voucherno.isBlank()) {
			return ResponseEntity.ok("{status:0,message:\"Bank Voucher No. required\"}");
		}
		if (bankid.isBlank()) {
			return ResponseEntity.ok("{status:0,message:\"Bank is required\"}");
		}
		Tuple t = db.getSingleResult("select top 1 * from " + table + " where voucherno=? and bankid=? and approved=1",
				Arrays.asList(voucherno, bankid));
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
				data.put("voucherno", t.get("voucherno") + "");
				data.put("taxpayername", t.get("taxpayername") + "");
				data.put("taxpayerpan", t.get("taxpayerpan") + "");
				data.put("depositedby", t.get("depositedby") + "");
				data.put("depcontact", t.get("depcontact") + "");
				data.put("lgid", t.get("lgid") + "");
				data.put("collectioncenterid", t.get("collectioncenterid") + "");
				data.put("accountno", t.get("accountno") + "");
				data.put("revenuecode", t.get("revenuecode") + "");
				data.put("purpose", t.get("purpose") + "");
				data.put("amount", t.get("amount") + "");
				data.put("bankid", t.get("bankid") + "");
				data.put("branchid", t.get("branchid") + "");
				data.put("creatorid", t.get("creatorid") + "");
				data.put("approved", t.get("approved") + "");
				data.put("approverid", t.get("approverid") + "");
				data.put("updatedon", t.get("updatedon") + "");
				data.put("chequebank", t.get("chequebank") + "");
				data.put("chequeamount", t.get("chequeamount") + "");
				data.put("chequeno", t.get("chequeno") + "");
				data.put("cstatus", t.get("cstatus") + "");
				data.put("ttype", t.get("ttype") + "");
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
		String sql = "select  dbo.eng2nep(dbo.getfiscalyear(date)) as fy,dbo.getrs(cast(tv.amount as float)) as amountwords,lls.namenp as llgname, bi.namenp, ba.accountname,karobarsanket as voucherno,karobarsanket,taxpayername, dbo.eng2nep(amount) as amount,dbo.eng2nep(accountno) as accountno,dbo.eng2nep(depcontact) as depcontact ,dbo.eng2nep(taxpayerpan) as taxpayerpan, dbo.eng2nep(dbo.getnepdate(cast(date as date))) as date, dbo.eng2nep(revenuecode) as revenuecode from "
				+ "taxvouchers tv " + "left join bankaccount ba on ba.id=tv.accountno "
				+ "left join bankinfo bi on bi.id=tv.bankid "
				+ "left join admin_local_level_structure lls on lls.id=tv.lgid "
//				+ "join crevenue cr on cr.code=tv.revenuecode "
				+ "where karobarsanket=? and tv.lgid=? ";
//		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(voucher, palika));
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
				+ "       total_amount_no.totalamountno as total_amount_no " + "FROM taxvouchers_detail tvd  "
				+ "JOIN taxvouchers tv ON tv.id = tvd.mainid  " + "JOIN crevenue cr ON cr.code = tvd.revenueid  "
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

		String sql = "select cast((format(getdate(),'yyyyMMdd')) as numeric) as today,bd.isused,bd.dateint,cast(bd.lgid as varchar) as lgid,cast(bd.id as varchar) as id,bd.amount,cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.accountno, bd.revenuetitle, "
				+ " bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd  join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid " + "where bd.karobarsanket=?";
		

//		System.out.println(sql);
		Map<String, Object> t = db.getSingleResultMap(sql,Arrays.asList(voucherno));
		
		if(t==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
		}
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(t.get("id")+""));
		t.put("revs", revs);
		if((t.get("isused")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Already used voucher").error();
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
		if (voucher.startsWith("{")) {
			voucher = "[" + voucher + "]";
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
//							System.out.println("here i am ");
							JSONObject ups = api.saveVoucherUpdates(id,taxpayername,taxpayerpan,amount);
							if(ups!=null) {
								if(ups.getInt("status")==1) {
									System.out.println("hello");
									db.execute("update "+table+" set taxpayername=? ,taxpayerpan=?,amount=? where id=?",Arrays.asList(taxpayername,taxpayerpan,amount,id));
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
							JSONObject ups =  api.saveVoucherUpdates(id,taxpayername,taxpayerpan,"");
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

		String sql = "select cast((format(getdate(),'yyyyMMdd')) as numeric) as today,cast(bankaccount.accountname as varchar) as accountname,bd.hasChangeReqest,bd.dateint,cast(bd.lgid as varchar) as lgid,cast(bd.id as varchar) as id,bd.amount,cast (bd.date as date) as date, bd.voucherno, "
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.accountno, bd.revenuetitle, "
				+ " bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby "
				+ "from taxvouchers as bd  join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ " join bankaccount on bankaccount.id=bd.accountno join admin_local_level_structure lls on lls.id = bd.lgid " + "where bd.karobarsanket=? ";

//		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(voucherno));
		if(data==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
		}
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(data.get("id")+""));
//		System.out.println(revs);
		data.put("revs", revs);
		if((data.get("isused")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Already used voucher").error();
		}
		if((data.get("hasChangeReqest")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Change request is already in process.").error();
		}
		
		JSONObject sdata = api.getVoucherDetails(data.get("id")+"");
		if(sdata!=null) {
			try {
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
		String acno=request("accountno");
		String remarks=request("remarks");
		if(id.isBlank() ||lgid.isBlank() || ccid.isBlank() || acno.isBlank()|| remarks.isBlank()) {
			return Messenger.getMessenger().setMessage("Required fields are not supplied.").error();
		}
		
		String sql = "select  top 1 *,cast((format(getdate(),'yyyyMMdd')) as numeric) as today from "+table+" where id=? and bankid=? and branchid=?";
		Map<String,Object> t = db.getSingleResultMap(sql,Arrays.asList(id,auth.getBankId(),auth.getBranchId()));
		if(t==null) {
			return Messenger.getMessenger().setMessage("No such voucher found.").error();
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
					} else {
						String llid = db.newIdInt();
						JSONObject pdata = api.sendDataToSutraPalikachange(id,llid, lgid, ccid, acno,remarks,auth.getUserId());
						if(pdata!=null) {
							if(pdata.getInt("status")==1) {
								db.execute("update "+table+" set hasChangeReqest=1 where id=?",Arrays.asList(id));
								db.execute("insert into taxvoucher_ll_change(id,vrefid,lgid,collectioncenterid,bankorgid,remarks,creatorid) values (?,?,?,?,?,?,?)",
										Arrays.asList(llid,id,lgid,ccid,acno,remarks,auth.getUserId()));
								return Messenger.getMessenger().setMessage("Voucher change requst sent.").success();
							}
						}
					}
				}
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
		String approve = request("approve");
		System.out.println("The approval Id is:" + approve);
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
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.accountno, bd.revenuetitle, "
				+ "bd.amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby ")
				.sqlBody(" from taxvouchers as bd join collectioncenter cc on cc.id = bd.collectioncenterid join admin_local_level_structure lls on lls.id = bd.lgid " + condition).paginate();
//		System.out.println(result);
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getdayclose() {
		String date=request("date");
		String lgid=request("lgid");
		String acno=request("acno");
		String sql="select ROW_NUMBER() OVER (ORDER BY taxvouchers.id) as sn, id, karobarsanket,taxpayername,amount from taxvouchers where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(lgid, acno));
		if(admlvl.isEmpty()) {
			return Messenger.getMessenger().setMessage("No transaction found").error();
		}else {
			List<Map<String, Object>> list = new ArrayList<>();
			if (!admlvl.isEmpty()) {
				for (Tuple t : admlvl) {
//					System.out.println(t.toString());
					Map<String, Object> mapadmlvl = new HashMap<>();
					mapadmlvl.put("id", t.get("id"));
					mapadmlvl.put("sn", t.get("sn"));
					mapadmlvl.put("karobarsanket", t.get("karobarsanket"));
					mapadmlvl.put("amount", t.get("amount"));
					mapadmlvl.put("taxpayername", t.get("taxpayername"));
					
					list.add(mapadmlvl);
				}
		}
			return Messenger.getMessenger().setData(list).success();
	}
//		return null;
}

	public ResponseEntity<Map<String, Object>> submitdayclose() {
		// TODO Auto-generated method stub
		String date=request("date");
		String lgid=request("lgid");
		String acno=request("acno");
		String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
		Map<String,Object> t = db.getSingleResultMap(sq,Arrays.asList(lgid,acno));
//		System.out.println(t.get("cid"));
		if(t.get("cid").toString().equals("1")) {
			return Messenger.getMessenger().setMessage("Day close has been done already").error();
		}
		if(date.isBlank() ||lgid.isBlank() || acno.isBlank()) {
			return Messenger.getMessenger().setMessage("Required field is not supplied.").error();
		}
		String sql = "insert into dayclose(lgid,accountno,creatorid,dateint) values (?,?,?,format(getdate(),'yyyyMMdd')) ";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(lgid,acno,auth.getUserId()));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		}else {
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
				+ "lls.namenp as llsname,cc.namenp as collectioncentername, " + "bd.accountno, bd.revenuetitle, "
				+ "amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby, "
				+" als.namenp as tlnamenp,tcc.namenp as tcnamenp,ba.accountnumber,ba.accountname,bat.accountnumber as taccountnumber,bat.accountname as taccountname"
				+ " from taxvouchers as bd  "
				+ "join collectioncenter cc on cc.id = bd.collectioncenterid  "
				+ "join admin_local_level_structure lls on lls.id = bd.lgid "
				+ "join taxvoucher_ll_change llc on llc.vrefid = bd.id "
				+ "join admin_local_level_structure als on als.id = llc.lgid "
				+ "join collectioncenter tcc on tcc.id = llc.collectioncenterid "
				+ "join bankaccount bat on bat.id = llc.bankorgid "
				+ "join bankaccount ba on ba.id = bd.accountno "
				+ "where bd.id=?";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(id));
		List<Map<String, Object>> revs = db.getResultListMap(
				"select td.revenueid,cr.namenp,td.amount from taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",
				Arrays.asList(id));
		data.put("revs", revs);
		Tuple t = db.getSingleResult("select * from taxvoucher_ll_change where vrefid=? and caseterminated=0",Arrays.asList(id));
		Map<String,Object> msg = new HashMap<>();
		if(t!=null) {
			if((t.get("palikaresponse")+"").equals("0")) {
				JSONObject presp = api.getPalikaResponse(id);
				if(presp!=null) {
					try {
						if(presp.getInt("status")==1) {
							JSONObject sdata = presp.getJSONObject("data");
							int repStatus = sdata.getInt("palikaresponse");
							String reason = sdata.getString("responsereason");
							if(repStatus==0) {
								msg.put("palikaresponse", "0");
								msg.put("responsereason", "");
							}else {
								db.execute("update taxvoucher_ll_change set palikaresponse=? ,responsereason=? where vrefid=? and caseterminated=0",Arrays.asList(repStatus,reason,id));
								msg.put("palikaresponse", repStatus);
								msg.put("responsereason", reason);
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
		Tuple t = db.getSingleResult("select top 1 * from taxvoucher_ll_change where vrefid=? and caseterminated=0",Arrays.asList(id));
		if(t!=null) {
			try {
			if((t.get("palikaresponse")+"").equals("2")) {
				
					JSONObject presp = api.settlePalikaChange(id,t.get("id")+"");
					if(presp!=null) {
						if(presp.getInt("status")==1) {
							db.execute("update "+table+" set hasChangeReqest=0,lgid=?,collectioncenterid=?,accountno=? where id=?",Arrays.asList(t.get("lgid"),t.get("collectioncenterid"),t.get("bankorgid"),id));
							db.execute("update taxvoucher_ll_change set caseterminated=1 ,terminationdate=getDate() where vrefid=? and caseterminated=0",Arrays.asList(id));
							return Messenger.getMessenger().setMessage("Data Successfully updated").success();
						}
					}else {
						return Messenger.getMessenger().setMessage("Error on communication with SuTRA ").error();
					}
			}else if((t.get("palikaresponse")+"").equals("3")) {
				JSONObject presp = api.settlePalikaChange(id,t.get("id")+"");
				if(presp!=null) {
					if(presp.getInt("status")==1) {
						db.execute("update "+table+" set hasChangeReqest=0 where id=?",Arrays.asList(id));
						db.execute("update taxvoucher_ll_change set caseterminated=1,terminationdate=getDate() where vrefid=? and caseterminated=0 and id=?",Arrays.asList(id,t.get("id")));
						return Messenger.getMessenger().setMessage("Data Successfully updated").success();
					}
				}else {
					return Messenger.getMessenger().setMessage("Error on communication with SuTRA ").error();
				}
			}
			}catch (JSONException e) {
				// TODO: handle exception
			}
		}
		return Messenger.getMessenger().setMessage("Invalid Request..").error();
	}
	

}