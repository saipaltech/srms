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
			condition += " and approved='"+approve+"'";
		}

		String sort = "";
		if (!request("sortKey").isBlank()) {
			if (!request("sortDir").isBlank()) {
				sort = request("sortKey") + " " + request("sortDir");
			}
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("cast(id as char) as id,cast(date as date) as date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,amount")
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

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("cast(id as char) as id,cast(date as date) as date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,chequeamount as amount")
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
		String sql = "select cast(bd.id as varchar) as id,cast (bd.date as date) as date, bd.voucherno,\r\n"
				+ "lls.namenp as llsname,cc.namenp as collectioncentername,\r\n" + "bd.accountno, bd.revenuetitle,\r\n"
				+ "SUM(t2.amount) as amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby\r\n"
				+ "from taxvouchers as bd left JOIN taxvouchers_detail t2 ON bd.id = t2.mainid join collectioncenter cc on cc.id = bd.collectioncenterid \r\n"
				+ "join admin_local_level_structure lls on lls.id = bd.lgid\r\n" + "where bd.id=" + id
				+ " group by bd.id,bd.date,bd.voucherno,lls.namenp,cc.namenp,bd.accountno,bd.revenuetitle, bd.purpose,bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby";
//		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql);
		List<Map<String,Object>> revs = db.getResultListMap("select td.revenueid,cr.namenp,td.amount form taxvouchers_detail td join taxvouchers t on t.id=td.mainid join crevenue cr on cr.id=td.revenueid where td.mainid=?",Arrays.asList(id));
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
		String usq = "select count(voucherno) from taxvouchers where voucherno=? and bankid=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.voucherno, auth.getBankId()));
		if ((!(res.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("This voucherno is already in use.").error();
		}
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
		sql = "INSERT INTO taxvouchers (id,date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,amount,creatorid, bankid, branchid,ttype,chequebank,chequeno,chequeamount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(id, model.date, model.voucherno, model.taxpayername, model.taxpayerpan, model.depositedby,
						model.depcontact, model.lgid, model.collectioncenterid, model.accountno, model.revenuecode,
						model.purpose, model.amount, auth.getUserId(), auth.getBankId(), auth.getBranchId(),
						model.ttype, model.chequebank, model.chequeno, model.chequeamount));
		if (rowEffect.getErrorNumber() == 0) {
			if (jarr.length() > 0) {
				for (int i = 0; i < jarr.length(); i++) {
					JSONObject objects = jarr.getJSONObject(i);
					String sq1 = "INSERT INTO taxvouchers_detail (did,mainid,revenueid,amount) values(?,?,?,?)";
					db.execute(sq1, Arrays.asList(db.newIdInt(), id, objects.get("rc"), objects.get("amt")));
				}
			}
			return Messenger.getMessenger().success();
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
			System.out.println();
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
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
		Tuple c = db.getSingleResult("select id,amount,approved from "+table+" where id=?",Arrays.asList(id));
		if((c.get("approved")+"").equals("1")) {
			return Messenger.getMessenger().setMessage("Voucher is already Approved.").error();
		}
		Tuple u = db.getSingleResult("select id,amountlimit,permid from users where id=?",Arrays.asList(auth.getUserId()));
		if((u.get("permid")+"").equals("3")) {
			if(!(u.get("amountlimit")+"").equals("-1")) {
				if(Float.parseFloat(c.get("amount")+"")>Float.parseFloat(u.get("amountlimit")+"")) {
					return Messenger.getMessenger().setMessage("Amount Limit exceeds, Only upto Rs."+u.get("amountlimit")+" is allowed.").error();
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
				revs.substring(0, revs.length() - 1);
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
		System.out.println(auth.getBankId());
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
		System.out.println(auth.getBankId());
		List<Tuple> d = db.getResultList(
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? order by als.namenp",
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
				revs.substring(0, revs.length() - 1);
			}
			try {
				JSONObject data = new JSONObject();
				data.put("id",t.get("id")+"");
				data.put("date",t.get("date")+"");
				data.put("voucherno",t.get("voucherno")+"");
				data.put("taxpayername",t.get("taxpayername")+"");
				data.put("taxpayerpan",t.get("taxpayerpan")+"");
				data.put("depositedby",t.get("depositedby")+"");
				data.put("depcontact",t.get("depcontact")+"");
				data.put("lgid",t.get("lgid")+"");
				data.put("collectioncenterid",t.get("collectioncenterid")+"");
				data.put("accountno",t.get("accountno")+"");
				data.put("revenuecode",t.get("revenuecode")+"");
				data.put("purpose",t.get("purpose")+"");
				data.put("amount",t.get("amount")+"");
				data.put("bankid",t.get("bankid")+"");
				data.put("branchid",t.get("branchid")+"");
				data.put("creatorid",t.get("creatorid")+"");
				data.put("approved",t.get("approved")+"");
				data.put("approverid",t.get("approverid")+"");
				data.put("updatedon",t.get("updatedon")+"");
				data.put("revenue",revs);
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
				+ "taxvouchers tv " + "left join bankaccount ba on ba.accountnumber=tv.accountno "
				+ "left join bankinfo bi on bi.id=tv.bankid "
				+ "left join admin_local_level_structure lls on lls.id=tv.lgid "
//				+ "join crevenue cr on cr.code=tv.revenuecode "
				+ "where voucherno=? and tv.lgid=? ";
//		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(voucher, palika));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> getRevenueDetails() {
		String voucher = request("voucherno");
		String palika = request("palika");
		String sql = "SELECT cr.namenp as revenuetitle,\r\n"
				+ "       dbo.eng2nep(ROW_NUMBER() OVER (ORDER BY tvd.revenueid)) as sn, \r\n"
				+ "       dbo.getrs(cast(tvd.amount as float)) as amountwords, \r\n"
				+ "       dbo.eng2nep(tvd.amount) as amount, \r\n"
				+ "       dbo.eng2nep(tvd.revenueid) as revenuecode,\r\n"
				+ "       total_amount.amountwords as total_amount,\r\n"
				+ "       total_amount_no.totalamountno as total_amount_no\r\n" + "FROM taxvouchers_detail tvd \r\n"
				+ "JOIN taxvouchers tv ON tv.id = tvd.mainid \r\n" + "JOIN crevenue cr ON cr.code = tvd.revenueid \r\n"
				+ "CROSS APPLY (\r\n" + "    SELECT dbo.getrs(cast(sum(amount) as float)) as amountwords\r\n"
				+ "    FROM taxvouchers_detail\r\n" + "    WHERE mainid = tv.id\r\n" + ") as total_amount\r\n"
				+ "CROSS APPLY (\r\n" + "    SELECT dbo.eng2nep(cast(sum(amount) as float)) as totalamountno\r\n"
				+ "    FROM taxvouchers_detail\r\n" + "    WHERE mainid = tv.id\r\n" + ") as total_amount_no\r\n"
				+ "WHERE tv.voucherno = ? AND tv.lgid = ?;";

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

}