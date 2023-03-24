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
public class BranchService extends AutoService{
	@Autowired
	DB db;
	@Autowired
	Authenticated auth;
	
private String table = "branches";
	

public ResponseEntity<Map<String, Object>> index() {
	String condition = "";
	if (!request("searchTerm").isEmpty()) {
		List<String> searchbles = Branch.searchables();
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
			.select("id,name,orgid, approved,disabled")
			.sqlBody("from " + table + condition).paginate();
	if (result != null) {
		return ResponseEntity.ok(result);
	} else {
		return Messenger.getMessenger().error();
	}
}

public ResponseEntity<Map<String, Object>> store() {
	String sql = "";
	Branch model = new Branch();
	model.loadData(document);
	sql = "INSERT INTO branches(id,name, orgid, disabled, approved) VALUES (?,?,?,?,?)";
	DbResponse rowEffect = db.execute(sql,
			Arrays.asList(model.id, model.name, model.orgid, model.disabled , model.approved));

	if (rowEffect.getErrorNumber() == 0) {
		return Messenger.getMessenger().success();

	} else {
		return Messenger.getMessenger().error();
	}
}

public ResponseEntity<Map<String, Object>> edit(String id) {

	String sql = "select id,name,orgid, disabled, approved from "
			+ table + " where id=?";
	Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
	return ResponseEntity.ok(data);
}

public ResponseEntity<Map<String, Object>> update(String id) {
	DbResponse rowEffect;
	Branch model = new Branch();
	model.loadData(document);

	String sql = "UPDATE name=?, where id=?";
	rowEffect = db.execute(sql,
			Arrays.asList(model.name));
	if (rowEffect.getErrorNumber() == 0) {
		return Messenger.getMessenger().success();

	} else {
		return Messenger.getMessenger().error();
	}

}

public ResponseEntity<Map<String, Object>> destroy(String id) {

	String sql = "delete from branches where id  = ?";
	DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
	if (rowEffect.getErrorNumber() == 0) {
		return Messenger.getMessenger().success();

	} else {
		return Messenger.getMessenger().error();
	}
}

	

}
