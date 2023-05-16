package org.saipal.srms.banks;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.ApiManager;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

@Component
public class BankService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	@Autowired
	ApiManager api;

	private String table = "bankinfo";

	public ResponseEntity<Map<String, Object>> index() {
		if (!auth.hasPermission("*")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where bi.id!=1 ";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Bank.searchables();
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
				.select("bi.id,bi.code,bi.namenp,bi.nameen,bi.approved,bi.disabled").sqlBody("from bankinfo bi join branches bn on bn.bankid = bi.id and ishead=1"  + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> store() {
		if (!auth.hasPermission("*")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sql = "";
		Bank model = new Bank();
		model.loadData(document);
//		String usq = "select count(code) from banks where code=?";
//		Tuple res = db.getSingleResult(usq, Arrays.asList(model.code));
//		if ((!(res.get(0) + "").equals("0"))) {
//			return Messenger.getMessenger().setMessage("Bank already exists.").error();
//		}
//		sql = "INSERT INTO banks(code,name, disabled, approved) VALUES (?,?,?,?)";
//		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.code, model.name, model.approved, model.disabled));
		
		String usq = "select count(id) from branches where bankid=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.bankid));
		if ((!(res.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("Bank already exists.").error();
		}
		
		String sql1 = "select id,code,namenp ,disabled,approved from bankinfo where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql1, Arrays.asList(model.bankid));
		
			sql = "INSERT INTO branches(name,bankid, disabled, approved,ishead) VALUES (?,?,?,?,?)";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(data.get("namenp")+" Head Branch",model.bankid,model.disabled,model.approved, 1));
			if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id, code, name ,disabled,approved from " + table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		if (!auth.hasPermission("*")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		Bank model = new Bank();
		model.loadData(document);
		String sql = "UPDATE " + table + " set approved=?, disabled=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.code, model.approved, model.disabled, model.name));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!auth.hasPermission("*")) {
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

	public ResponseEntity<List<Map<String,Object>>> getList() {
		String bankId = auth.getBankId();
		String sql = "";
		if (bankId.equals("1")) {
			sql = "select id,namenp from " + table + " where id !=1 and id not in (select distinct bankid from branches)";
		} else {
			sql = "select id,namenp from " + table + " where id ='" + bankId + "' and id not in (select distinct bankid from branches)";
		}
		return ResponseEntity.ok(db.getResultListMap(sql));
	}

	public ResponseEntity<List<Map<String,Object>>> getBanksFromSutra() {
		String sql = "select id,namenp from bankinfo where id !=1 and id in(select bankid from branches where ishead=1 and bankid=?)";
		return ResponseEntity.ok(db.getResultListMap(sql,Arrays.asList(auth.getBankId())));
//		return ResponseEntity.ok(api.getBanks().toString());
	}

	public ResponseEntity<List<Map<String, Object>>> getDistrict() {
		String sql = "select districtid as id,namenp from admin_district as a "
				+ " inner join (select distinct a.districtid as did from admin_local_level_structure as a inner join bankaccount as b on a.id=b.lgid "
				+ " where b.approved=1 and b.disabled=0 and b.bankid=? )  as b on a.districtid=b.did order by namenp ";

		return ResponseEntity.ok(db.getResultListMap(sql,Arrays.asList(auth.getBankId())));
	}
	
	public ResponseEntity<List<Map<String, Object>>> getPalika() {
		String did=request("did");
		String sql = "select cast(a.id as varchar) as code, a.namenp as name from "
				+ " admin_local_level_structure as a inner join "
				+ " (select distinct lgid from  bankaccount  where  approved=1 and disabled=0 and  bankid=? ) as b "
				+ " on a.id=b.lgid  where districtid=? order by name ";
		System.out.println("\n\n"+sql+"\n\n");
		return ResponseEntity.ok(db.getResultListMap(sql,Arrays.asList(auth.getBankId(),did)));
	}

}