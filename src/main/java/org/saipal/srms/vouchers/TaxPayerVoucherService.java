package org.saipal.srms.vouchers;

import java.util.Arrays;

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
		String condition = " where id!=1 ";
		String approve = request("approvelog");
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = TaxPayerVoucher.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);

		
				switch (Integer.parseInt(approve)) {
				case 0:
					condition += " where approved=0)";
					break;
				case 1:
					condition += " where approved=1)";
					break;
				default:
					condition += "where approved=1)";
					break;
				}

		}
		String sort = "";
		if (!request("sortKey").isBlank()) {
			if (!request("sortDir").isBlank()) {
				sort = request("sortKey") + " " + request("sortDir");
			}
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select("id,cast(date as date) as date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,amount")
				.sqlBody("from " + table + condition).paginate();
		System.out.println(result);
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<List<Map<String, Object>>> getSpecific(String id) {
		//String transactionid = request("id");
//		String sql = "select bd.id, bd.depositdate, bd.bankvoucherno, lls.namenp as llsname,cc.namenp as collectioncentername, bd.accountnumber, bd.amount, bd.remarks, bd.transactionid from bank_deposits as bd join collectioncenter cc on cc.id = bd.collectioncenterid join admin_local_level_structure lls on lls.id = bd.lgid where bd.id =" + id;
		String sql = "select bd.id,cast (bd.date as date) as date, bd.voucherno,\r\n"
				+ "lls.namenp as llsname,cc.namenp as collectioncentername,\r\n"
				+ "bd.accountno, bd.revenuetitle,\r\n"
				+ "bd.amount, bd.purpose, bd.taxpayerpan, bd.taxpayername, bd.depcontact, bd.depositedby\r\n"
				+ "from taxvouchers as bd join collectioncenter cc on cc.id = bd.collectioncenterid \r\n"
				+ "join admin_local_level_structure lls on lls.id = bd.lgid\r\n"
				+ "where bd.id="+ id;
		System.out.println(sql);
		return ResponseEntity.ok(db.getResultListMap(sql));
	}
	

	public ResponseEntity<Map<String, Object>> store() throws JSONException {
		if (!auth.hasPermission("bankuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String voucher=request("voucherinfo");
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
		String id = db.newIdInt();
		sql = "INSERT INTO taxvouchers (id,date,voucherno,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,accountno,revenuecode,purpose,amount,creatorid, bankid, branchid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(id, model.date, model.voucherno, model.taxpayername, model.taxpayerpan, model.depositedby,
						model.depcontact, model.lgid, model.collectioncenterid, model.accountno, model.revenuecode,
						model.purpose, model.amount, auth.getUserId(), auth.getBankId(), auth.getBranchId()));
		if (rowEffect.getErrorNumber() == 0) {
			if (jarr.length() > 0) {
				for (int i = 0; i < jarr.length(); i++) {
					JSONObject objects = jarr.getJSONObject(i);
				
						String sq1 = "INSERT INTO taxvouchers_detail (did,mainid,revenueid,amount) values(?,?,?,?)";
						db.execute(sq1, Arrays.asList(db.newIdInt(),id, objects.get("rc"), objects.get("amt")));
					

				}

			}
			try {
				JSONObject obj = api.sendDataToSutra(model, id, auth.getBankId(), auth.getBranchId(), auth.getUserId());
				if (obj != null) {
					if (obj.getInt("status") == 1) {
						db.execute("update taxvouchers set syncstatus=2 where voucherno='" + model.voucherno + "'");
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
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? order by als.namenp",
				Arrays.asList(auth.getBankId()));
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
		String revenueCode = request("revenuecode");
		if (llgCode.isBlank() && revenueCode.isBlank()) {
			return ResponseEntity.ok("{\"status\":0,\"message\":\"Local Level & Revenuecode is required\"}");
		}
		// internal
		int type = 9;
		if (Integer.parseInt(revenueCode) > 33300) {
			// sharing
			type = 10;
		}
		// check if data is cached
		List<Tuple> d = db.getResultList(
				"select ba.accountname,ba.accountnumber from bankaccount ba where ba.bankid=? and ba.lgid=? and ba.accounttype=?",
				Arrays.asList(auth.getBankId(), llgCode, type));
		if (d.size() > 0) {
			try {
				JSONObject j = new JSONObject();
				JSONArray dt = new JSONArray();
				for (Tuple t : d) {
					dt.put(Map.of("acno", t.get("accountnumber") + "", "name", t.get("accountname")));
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
		// check if data is cached
		List<Tuple> d = db.getResultList("select code,namenp from crevenue where 1=1");
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
		Tuple t = db.getSingleResult("select top 1 * from " + table + " where voucherno=? and bankid=?",
				Arrays.asList(voucherno, bankid));
		if (t != null) {
			try {
				JSONObject data = new JSONObject();
				data.put("id", t.get("id"));
				data.put("date", t.get("date"));
				data.put("voucherno", t.get("voucherno"));
				data.put("taxpayername", t.get("taxpayername"));
				data.put("taxpayerpan", t.get("taxpayerpan"));
				data.put("depositedby", t.get("depositedby"));
				data.put("depcontact", t.get("depcontact"));
				data.put("lgid", t.get("lgid"));
				data.put("collectioncenterid", t.get("collectioncenterid"));
				data.put("accountno", t.get("accountno"));
				data.put("revenuecode", t.get("revenuecode"));
				data.put("purpose", t.get("purpose"));
				data.put("amount", t.get("amount"));
				data.put("bankid", t.get("bankid"));
				data.put("branchid", t.get("branchid"));
				data.put("creatorid", t.get("creatorid"));
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
		if(panno.isBlank()) {
			return Messenger.getMessenger().setMessage("Pan No. is Required").error();
		}
		try {
			JSONArray dt = pan.getData(panno);
			if(dt!=null) {
				JSONObject jb = dt.getJSONObject(0);
				return Messenger.getMessenger().setData(Map.of("taxpayer",jb.getString("taxpayer"),"contactNo",jb.getString("contactNo")==null?"":jb.getString("contactNo"))).success();
			}
			return Messenger.getMessenger().setMessage("Invalid Pan No.").error();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return Messenger.getMessenger().setMessage("Invalid Pan No.").error();
		}
	}
		
	public ResponseEntity<Map<String, Object>> generateReport() {
		String voucher = request("voucherno");
		String palika = request("palika");
		String sql = "select cr.namenp as revenuetitle, dbo.eng2nep(dbo.getfiscalyear(date)) as fy,dbo.getrs(cast(tv.amount as float)) as amountwords,lls.namenp as llgname, bi.namenp, ba.accountname,dbo.eng2nep(voucherno) as voucherno,karobarsanket,taxpayername, dbo.eng2nep(amount) as amount,dbo.eng2nep(accountno) as accountno,dbo.eng2nep(depcontact) as depcontact ,dbo.eng2nep(taxpayerpan) as taxpayerpan, dbo.eng2nep(dbo.getnepdate(cast(date as date))) as date, dbo.eng2nep(revenuecode) as revenuecode from "
				+ "taxvouchers tv "
				+ "join bankaccount ba on ba.accountnumber=tv.accountno "
				+ "join bankinfo bi on bi.id=tv.bankid "
				+ "join admin_local_level_structure lls on lls.id=tv.lgid "
				+ "join crevenue cr on cr.code=tv.revenuecode "
				+ "where voucherno=? and tv.lgid=? ";
		System.out.println(sql);
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(voucher, palika));
		return ResponseEntity.ok(data);
	}

}