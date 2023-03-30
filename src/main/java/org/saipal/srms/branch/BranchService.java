package org.saipal.srms.branch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BranchService extends AutoService {
	@Autowired
	DB db;
	@Autowired
	Authenticated auth;

	private String table = "branches";

	public ResponseEntity<Map<String, Object>> index() {
		if(!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition="";
		String bankId = auth.getBankId();
		if (bankId.equals("1")) {
			condition = " where id!=1 ";
		} else {
			condition = " where bankid='"+bankId+"' ";
		}
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Branch.searchables();
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
				.select("id,name,bankid, approved,disabled").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		if(!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sql = "";
		Branch model = new Branch();
		model.loadData(document);
		sql = "INSERT INTO branches(name, bankid,code, disabled, approved) VALUES (?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.bankid, model.code,model.disabled, model.approved));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {
		String sql = "select id,name,code,bankid, disabled, approved from " + table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		if(!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		DbResponse rowEffect;
		Branch model = new Branch();
		model.loadData(document);
		String sql = "UPDATE branches set name=?,code=?,disabled=?,approved=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.name,model.code,model.disabled,model.approved,model.id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if(!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sql = "delete from branches where id  = ?";
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
			sql = "select id,name from " + table + " where bankid ='" + bankId + "'";
		}
		return ResponseEntity.ok(db.getResultListMap(sql));
	}

}
