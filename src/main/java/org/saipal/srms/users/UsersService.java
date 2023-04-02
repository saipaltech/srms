package org.saipal.srms.users;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.persistence.Tuple;

@Component
public class UsersService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	@Autowired
	PasswordEncoder pe;

	private String table = "users";

	public ResponseEntity<Map<String, Object>> index() {
		if (!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = "";
		String bankId = auth.getBankId();
		if (bankId.equals("1")) {
			condition = " where 1=1 ";
		} else {
			condition = " where u.bankid='" + bankId + "' ";
		}
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Users.searchables();
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
				.select(" u.id,u.name,u.username,u.post, u.mobile ,branches.name as bname, u.approved,u.disabled")
				.sqlBody("from " + table + " as u join branches on u.branchid = branches.id " + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		if (!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String bankId = auth.getBankId();
		String sql = "";
		Users model = new Users();
		model.loadData(document);
		String usq = "select count(username) from users where username=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.username));
		if ((!(res.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("Username already exists.").error();
		}
		model.password = pe.encode(model.password);
		if (bankId.equals("1")) {
			bankId = db.getSingleResult("select bankid from branches where id=?", Arrays.asList(model.branchid)).get(0)
					+ "";
		}

		sql = "INSERT INTO users(name, post, username, password, mobile ,bankid, branchid , disabled, approved) VALUES (?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.post, model.username, model.password,
				model.mobile, bankId, model.branchid, model.disabled, model.approved));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> storeBankUser() {
//		String bankId = auth.getBankId();
		String sql = "";
		Users model = new Users();
		model.loadData(document);
		String usq = "select count(username) from users where username=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.username));
		if ((!(res.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("Username already exists.").error();
		}
		String sq="select count(id) from branches where bankid=? and ishead=1";
		Tuple resp = db.getSingleResult(sq, Arrays.asList(model.bankid));
		if ((!(resp.get(0) + "").equals("1"))) {
			return Messenger.getMessenger().setMessage("Headbranch does not exists.").error();
		}
		model.password = pe.encode(model.password);
		sql = "INSERT INTO users(name, post,username, password, mobile ,bankid, branchid ,disabled, approved) VALUES (?,?,?,?,?,?,(select top 1 id from branches where bankid=? and ishead=1),?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.post, model.username, model.password,
				model.mobile, model.bankid, model.bankid, model.disabled, model.approved));
		if (rowEffect.getErrorNumber() == 0) {
			String sqls = "Insert into users_perms (userid, permid) values((select top 1 id from users where username = ?), 2),((select top 1 id from users where username = ?), 3)";
			db.execute(sqls, Arrays.asList(model.username,model.username));
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,name, username, post, mobile ,branchid,disabled, approved from " + table
				+ " where id=?";

		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		if (!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		if (id.equals("1")) {
			return Messenger.getMessenger().setMessage("Cannot Edit System user").error();
		}
		DbResponse rowEffect;
		Users model = new Users();
		model.loadData(document);
		String sql = "UPDATE users set name=?, mobile=?,branchid=?,post=?,disabled=?, approved=? where id=?";
		rowEffect = db.execute(sql,

				Arrays.asList(model.name, model.mobile, model.branchid, model.post, model.disabled, model.approved,
						model.id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		if (!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String sql = "delete from users where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<List<Map<String, Object>>> getUserList(){
		String sql = "Select name, username, post, mobile from users where username="+ "'"+ request("username")+"'";
		return ResponseEntity.ok(db.getResultListMap(sql));
	}
	

}
