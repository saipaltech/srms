package org.saipal.srms.users;

import java.util.ArrayList;
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

import javax.persistence.Tuple;
import javax.transaction.Transactional;

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

	@Transactional
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
		String mbl = "select count(mobile) from users where username=?";
		Tuple resu = db.getSingleResult(mbl, Arrays.asList(model.mobile));
		if ((!(resu.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("Mobile already exists.").error();
		}
		model.password = pe.encode(model.password);
		if (bankId.equals("1")) {
			bankId = db.getSingleResult("select bankid from branches where id=?", Arrays.asList(model.branchid)).get(0)
					+ "";
		}

		sql = "INSERT INTO users(name, post, username, password,amountlimit, mobile ,bankid, branchid , disabled, approved) VALUES (?,?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.name, model.post, model.username, model.password,
						model.amountlimit.isBlank() ? 0 : model.amountlimit, model.mobile, bankId, model.branchid,
						model.disabled, model.approved));
		String permid = request("permid") + "";
		String sqls = "";
		DbResponse rowEffects;
		if (permid.equals("4")) {
			sqls = "insert into users_perms (userid,permid) values ((select top 1 id from users where username=?),?),((select top 1 id from users where username=?),3)";
			rowEffects = db.execute(sqls, Arrays.asList(model.username, permid, model.username));
		} else {
			sqls = "insert into users_perms (userid,permid) values ((select top 1 id from users where username=?),?)";
			rowEffects = db.execute(sqls, Arrays.asList(model.username, permid));
		}
		if (rowEffect.getErrorNumber() == 0 && rowEffects.getErrorNumber() == 0) {
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
		String sq = "select count(id) from branches where bankid=? and ishead=1";
		Tuple resp = db.getSingleResult(sq, Arrays.asList(model.bankid));
		if ((!(resp.get(0) + "").equals("1"))) {
			return Messenger.getMessenger().setMessage("Headbranch does not exists.").error();
		}
		model.password = pe.encode(model.password);
		sql = "INSERT INTO users(name, post,username,permid, password, mobile ,bankid, branchid ,disabled, approved) VALUES (?,?,?,?,?,?,?,(select top 1 id from branches where bankid=? and ishead=1),?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.post, model.username, model.password,
				model.permid, model.mobile, model.bankid, model.bankid, model.disabled, model.approved));
		if (rowEffect.getErrorNumber() == 0) {
			String sqls = "Insert into users_perms (userid, permid) values((select top 1 id from users where username = ?), 2),((select top 1 id from users where username = ?), 3)";
			db.execute(sqls, Arrays.asList(model.username, model.username));
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,name, username, post, amountlimit ,mobile, permid ,branchid,disabled, approved from "
				+ table + " where id=?";

		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	@Transactional
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
		String sql = "UPDATE users set name=?, mobile=?,branchid=?,post=?,permid=?, amountlimit=? ,disabled=?, approved=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.name, model.mobile, model.branchid, model.post, model.permid,
				model.amountlimit.isBlank() ? 0 : model.amountlimit, model.disabled, model.approved, model.id));
		String permid = request("permid") + "";
		String sqls = "";
		DbResponse rowEffects;
		db.execute("delete from users_perms where userid=?", Arrays.asList(id));
		if (permid.equals("4")) {
			sqls = "insert into users_perms (userid,permid) values (?,?),(?,3)";
			rowEffects = db.execute(sqls, Arrays.asList(model.id, permid, model.id));
		} else {
			sqls = "insert into users_perms (userid,permid) values (?,?)";
			rowEffects = db.execute(sqls, Arrays.asList(model.id, permid));
		}
		if (rowEffect.getErrorNumber() == 0 && rowEffects.getErrorNumber() == 0) {
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

	public ResponseEntity<List<Map<String, Object>>> getUserList() {
		String sql = "Select name, username, post, mobile, amountlimit from users where id=" + auth.getUserId();
		return ResponseEntity.ok(db.getResultListMap(sql));
	}

	public ResponseEntity<List<Map<String, Object>>> frontMenu() {
		List<String> exclude = new ArrayList<>();
		exclude.add("bank");
		exclude.add("branch");
		exclude.add("users");
		String sql = "";
		if (auth.hasPermissionOnly("*")) {
			sql = "select * from front_menu order by morder";
			return ResponseEntity.ok(db.getResultListMap(sql));
		}
		if (auth.hasPermissionOnly("bankhq")) {
			exclude.remove("branch");
			exclude.remove("users");
		}

		sql = "select * from front_menu where link not in ('" + String.join("','", exclude) + "') order by morder";
		return ResponseEntity.ok(db.getResultListMap(sql));
	}

	public ResponseEntity<Map<String, Object>> resetPassword(String id) {
		String password = request("password");
		String cpassword = request("cpassword");
		System.out.println("the password is:" + password);
		System.out.println("the cpassword is:" + cpassword);
		if (cpassword.equals(password)) {
			String sql = "update users set password = ? where id=" + id;
			DbResponse rowEffect = db.execute(sql, Arrays.asList(pe.encode(password)));
			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().setMessage("Password Changed Successfully").success();

			} else {
				return Messenger.getMessenger().setMessage("Pasword change unsuccessful").error();
			}
		}
		return Messenger.getMessenger().setMessage("Password and Confirm Passowrds do not match").error();

	}

	public ResponseEntity<Map<String, Object>> changePassword() {
		String password = request("password");
		String cpassword = request("cpassword");
		String oldpassword = request("oldpassword");
		if (!cpassword.equals(password))
			return Messenger.getMessenger().setMessage("Password and Confirm Passowrds do not match").error();
		Tuple t = db.getSingleResult("select password from users where id=" + auth.getUserId());
		if (t != null) {
			if (pe.matches(oldpassword, t.get(0) + "")) {
				DbResponse rowEffect = db
						.execute("update users set password='" + pe.encode(password) + "' where id=" + auth.getUserId());
				if (rowEffect.getErrorNumber() == 0) {
					return Messenger.getMessenger().setMessage("Password Changed Successfully").success();
				} else {
					return Messenger.getMessenger().setMessage("Pasword change unsuccessful").error();
				}
			} else {
				return Messenger.getMessenger().setMessage("Old Password Does not Match.").error();
			}
		}
		return Messenger.getMessenger().setMessage("User Does not Exist..").error();
	
	}

}
