package org.saipal.srms.organizations;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import jakarta.*;
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
public class OrganizationService extends AutoService{
	@Autowired
	DB db;
	@Autowired
	Authenticated auth;
	
private String table = "organizations";
	

	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Organization.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		if (!condition.isBlank()) {
			condition = " where 1=1 " + condition;
		}
		String sort = "";
		if(!request("sortKey").isBlank()) {
			if(!request("sortDir").isBlank()) {
				sort = request("sortKey")+" "+request("sortDir");
			}
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage"))
				.setOrderBy(sort)
				.select("id,code,name,approved,disabled")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		String sql = "";
		Organization model = new Organization();
		model.loadData(document);
		sql = "INSERT INTO organizations(code,name, disabled, approved) VALUES (?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.code, model.name, model.approved, model.disabled));
	
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id, code, name ,disabled from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Organization model = new Organization();
		model.loadData(document);

		String sql = "UPDATE code=?,approved=?, disabled=?, name=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.code, model.approved, model.disabled, model.name));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from "+ table +" where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}


}