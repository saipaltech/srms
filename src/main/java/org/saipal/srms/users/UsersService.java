package org.saipal.srms.users;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.settings.SettingsService;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UsersService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	@Autowired
	PasswordEncoder pe;

	@Autowired
	SettingsService ss;

	DataFormatter formatter = new DataFormatter();

	private String table = "users";

	public ResponseEntity<Map<String, Object>> index() {
		if (!(auth.hasPermission("bankhq") || auth.hasPermissionOnly("banksupervisor") || auth.canFromUserTable("7"))) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = "";
		String bankId = auth.getBankId();
		if (bankId.equals("1")) {
			condition = " where u.id in (select userid from users_perms where permid=2)";
		} else {
			condition = " where u.bankid='" + bankId + "' ";
		}
		if (!auth.hasPermissionOnly("bankhq")) {
			if (auth.hasPermissionOnly("banksupervisor")) {
				condition += " and u.branchid='" + auth.getBranchId() + "' ";
			}
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
				.select(" cast(u.id as varchar) as id,u.name,u.username,u.post, u.mobile ,branches.name as bname, u.approved,u.disabled")
				.sqlBody("from " + table + " as u join branches on u.branchid = branches.id " + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> indexAll() {
		String bankid=request("bankid");
		String branchid=request("branchid");
		
		if (!auth.hasPermissionOnly("loginuser")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String condition = " where 1=1 ";
		if(!bankid.equals("0")) {
			condition += " and u.bankid="+bankid+ " ";
		}
		if(!branchid.equals("0")) {
			condition += " and u.branchid="+branchid + " ";
		}
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Users.searchables();
			condition += " and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		System.out.println(condition);
		String sort = "";
		if (!request("sortKey").isBlank()) {
			if (!request("sortDir").isBlank()) {
				sort = request("sortKey") + " " + request("sortDir");
			}
		}

		Paginator p = new Paginator();

		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage")).setOrderBy(sort)
				.select(" cast(u.id as varchar) as id,u.name,u.username,u.post, u.mobile ,branches.name as bname, u.approved,u.disabled")
				.sqlBody("from " + table + " as u join branches on u.branchid = branches.id " + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> store() {
		if (!(auth.hasPermission("bankhq") || auth.hasPermissionOnly("banksupervisor") || auth.canFromUserTable("7"))) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String bankId = auth.getBankId();
		String sql = "";
		Users model = new Users();
		model.loadData(document);
		String usq = "select count(username) from users where username=?";
		Tuple res = db.getSingleResult(usq, Arrays.asList(model.username));
		if (!(res.get(0) + "").equals("0")) {
			return Messenger.getMessenger().setMessage("Username already exists.").error();
		}
		String mbl = "select count(mobile) from users where mobile=? and disabled=0";
		Tuple resu = db.getSingleResult(mbl, Arrays.asList(model.mobile));
		if (!(resu.get(0) + "").equals("0")) {
			return Messenger.getMessenger().setMessage("Mobile already exists.").error();
		}
		model.password = pe.encode(model.password);
		if (bankId.equals("1")) {
			bankId = db.getSingleResult("select bankid from branches where id=?", Arrays.asList(model.branchid)).get(0)
					+ "";
		}

		sql = "INSERT INTO users(name, post,permid, username, password,amountlimit, mobile,email ,bankid, branchid , disabled, approved) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.name, model.post, model.permid, model.username, model.password,
						model.amountlimit.isBlank() ? 0 : model.amountlimit, model.mobile, model.email, bankId,
						model.branchid, model.disabled, model.approved));
		String permid = request("permid") + "";
		if (rowEffect.getErrorNumber() == 0) {
			String sqls = "";
			DbResponse rowEffects;
			if (permid.equals("4")) {
				sqls = "insert into users_perms (userid,permid) values ((select top 1 id from users where username=?),?),((select top 1 id from users where username=?),3)";
				rowEffects = db.execute(sqls, Arrays.asList(model.username, permid, model.username));
			} else {
				sqls = "insert into users_perms (userid,permid) values ((select top 1 id from users where username=?),?)";
				rowEffects = db.execute(sqls, Arrays.asList(model.username, permid));
			}
			if (rowEffects.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();
			} else {
				return Messenger.getMessenger().error();
			}
		}
		return Messenger.getMessenger().error();
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
		String esq = "select count(id) from users where email=?";
		Tuple eres = db.getSingleResult(esq, Arrays.asList(model.email));
		if ((!(eres.get(0) + "").equals("0"))) {
			return Messenger.getMessenger().setMessage("Email already exists.").error();
		}
		String mbl = "select count(mobile) from users where mobile=?";
		Tuple resu = db.getSingleResult(mbl, Arrays.asList(model.mobile));
		if (!(resu.get(0) + "").equals("0")) {
			return Messenger.getMessenger().setMessage("Mobile already exists.").error();
		}
		String sq = "select count(id) from branches where bankid=? and ishead=1";
		Tuple resp = db.getSingleResult(sq, Arrays.asList(model.bankid));
		if ((!(resp.get(0) + "").equals("1"))) {
			return Messenger.getMessenger().setMessage("Headbranch does not exists.").error();
		}
		Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$");
		Matcher matcher = pattern.matcher(model.password);
		if (!matcher.matches()) {
			return Messenger.getMessenger().setMessage(
					"Password must have at least 8 characters with at least one special character, one Upper case charcater and one number.")
					.error();
		}
		model.password = pe.encode(model.password);
		sql = "INSERT INTO users(name, post,username,permid, password, mobile ,bankid, branchid ,disabled, approved,email) VALUES (?,?,?,'4',?,?,?,(select top 1 id from branches where bankid=? and ishead=1),?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.post, model.username, model.password,
				model.mobile, model.bankid, model.bankid, 0, 1, model.email));
		if (rowEffect.getErrorNumber() == 0) {
			String sqls = "Insert into users_perms (userid, permid) values((select top 1 id from users where username = ?), 2),((select top 1 id from users where username = ?), 3)";
			db.execute(sqls, Arrays.asList(model.username, model.username));
			return Messenger.getMessenger().success();
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select cast(id as varchar) as id,name, username, post, amountlimit ,mobile,email, permid ,branchid,disabled, approved from "
				+ table + " where id=?";

		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	@Transactional
	public ResponseEntity<Map<String, Object>> update(String id) {
		if (!(auth.hasPermission("bankhq") || auth.hasPermissionOnly("banksupervisor") || auth.canFromUserTable("7"))) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		if (id.equals("1") || id.equals("2")) {
			return Messenger.getMessenger().setMessage("Cannot Edit System user").error();
		}
		DbResponse rowEffect;
		Users model = new Users();
		model.loadData(document);

		String mbl = "select count(mobile) from users where mobile=? and id<>? and disabled=0";
		Tuple resu = db.getSingleResult(mbl, Arrays.asList(model.mobile, id));
		if (!(resu.get(0) + "").equals("0")) {
			return Messenger.getMessenger().setMessage("Mobile already exists.").error();
		}
		String eml = "select count(email) from users where email=? and id<>?";
		Tuple rese = db.getSingleResult(eml, Arrays.asList(model.email, id));
		if (!(rese.get(0) + "").equals("0")) {
			return Messenger.getMessenger().setMessage("Email already exists.").error();
		}
		String sql = "UPDATE users set name=?, mobile=?,email=?,branchid=?,post=?,permid=?, amountlimit=? ,disabled=?, approved=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.name, model.mobile, model.email, model.branchid, model.post, model.permid,
						model.amountlimit.isBlank() ? 0 : model.amountlimit, model.disabled, model.approved, model.id));
		String permid = request("permid") + "";
		String sqls = "";
		DbResponse rowEffects;

		db.execute("delete from users_perms where userid=? and permid<>2", Arrays.asList(id));
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
		if (!(auth.hasPermission("bankhq") || auth.hasPermissionOnly("banksupervisor") || auth.canFromUserTable("7"))) {
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

	public ResponseEntity<Object> frontMenu() {
		List<String> exclude = new ArrayList<>();
		exclude.add("bank");
		exclude.add("branch");
		exclude.add("users");
		exclude.add("approve-voucher");
		exclude.add("settings");
		exclude.add("all-users");
		String sql = "";
		if (auth.canFromUserTable("8")) {
			sql = "select * from front_menu where link in ('report') order by morder";
			return ResponseEntity.ok(db.getResultListMap(sql));
		}
		if (auth.canFromUserTable("7")) {
			sql = "select * from front_menu where link in ('branch','users') order by morder";
			return ResponseEntity.ok(db.getResultListMap(sql));
		}

		if (auth.hasPermissionOnly("*")) {
			String c = "";
			if (auth.hasPermissionOnly("loginuser")) {
				c = ",'all-users'";
			}
			sql = "select * from front_menu where link in ('bank','users'" + c + ") order by morder";
			return ResponseEntity.ok(db.getResultListMap(sql));
		}
		if (auth.hasPermissionOnly("bankhq")) {
			exclude.remove("branch");
			exclude.remove("users");
			exclude.remove("approve-voucher");
			exclude.remove("settings");
		}
		if (auth.hasPermissionOnly("banksupervisor")) {
			String ksetVal = ss.getSetting(SettingsService.supccuKey);
			if (ksetVal.equals("1") || ksetVal.isBlank()) {
				exclude.remove("users");
			}
			exclude.remove("approve-voucher");
		}
		
		List<Tuple> glt = db.getResultList(sql);
		Map<String,Menu> menu= new LinkedHashMap<>();
		sql = "select *,(case when groupid is not null then ((groupid*11)+morder) else morder end) as ord  from front_menu where link not in ('" + String.join("','", exclude) + "') order by ord";
		List<Tuple> lt = db.getResultList(sql);
		for(Tuple t : lt) {
			String gkey = t.get("groupid")==null?"":(t.get("groupid")+"");
			if(!gkey.isBlank()) {
				if(menu.containsKey(gkey)){
					menu.get(gkey).childs.add(new Menu(t.get("id")+"",t.get("name")+"",t.get("icon")+"",t.get("link")+""));
				}
			}else {
				menu.put(t.get("id")+"",new Menu(t.get("id")+"",t.get("name")+"",t.get("icon")+"",t.get("link")+""));
			}
		}
		return ResponseEntity.ok(menu.values().toArray());
	}

	public ResponseEntity<Map<String, Object>> resetPassword(String id) {
		String password = request("password");
		String cpassword = request("cpassword");
		if (cpassword.equals(password)) {
			Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$");
			Matcher matcher = pattern.matcher(password);
			if (!matcher.matches()) {
				return Messenger.getMessenger().setMessage(
						"Password must have at least 8 characters with at least one special character, one Upper case charcater and one number.")
						.error();
			}
			String sql = "update users set password = ?, pwchangedate=NULL where id=" + id;
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
		// ^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$
		String password = request("password");
		String cpassword = request("cpassword");
		String oldpassword = request("oldpassword");
		if (!cpassword.equals(password))
			return Messenger.getMessenger().setMessage("Password and Confirm Passowrds do not match").error();

		Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$");
		Matcher matcher = pattern.matcher(password);

		if (!matcher.matches()) {
			return Messenger.getMessenger().setMessage(
					"Password must have at least 8 characters with at least one special character, one Upper case charcater and one number.")
					.error();
		}

		Tuple t = db.getSingleResult("select password from users where id=" + auth.getUserId());
		if (t != null) {
			if (pe.matches(oldpassword, t.get(0) + "")) {
				DbResponse rowEffect = db.execute(
						"update users set password='" + pe.encode(password) + "' where id=" + auth.getUserId());
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

	public ResponseEntity<Map<String, Object>> changePasswordLogin() {
		// ^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$
		String password = request("password");
		String cpassword = request("cpassword");
		String oldpassword = request("oldpassword");
		String username = request("username");
		if (!cpassword.equals(password))
			return Messenger.getMessenger().setMessage("Password and Confirm Passowrds do not match").error();

		Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$");
		Matcher matcher = pattern.matcher(password);

		if (!matcher.matches()) {
			return Messenger.getMessenger().setMessage(
					"Password must have at least 8 characters with at least one special character, one Upper case charcater and one number.")
					.error();
		}

		Tuple t = db.getSingleResult("select id, password from users where username= ?", Arrays.asList(username));
		if (t != null) {
			if (pe.matches(oldpassword, t.get("password") + "")) {
				DbResponse rowEffect = db.execute("update users set password='" + pe.encode(password)
						+ "', pwchangedate=format(getdate(),'yyyyMMdd') where id=" + t.get("id"));
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

	public ResponseEntity<List<Map<String, Object>>> getUerTypes() {
		if (auth.hasPermission("bankhq")) {
			return ResponseEntity
					.ok(Arrays.asList(Map.of("id", 3, "name", "Bank User"), Map.of("id", 4, "name", "Supervisor"),
							Map.of("id", 7, "name", "Technical User"), Map.of("id", 8, "name", "Monitoring User")));
		}
		if (auth.canFromUserTable("4")) {
			String ksetVal = ss.getSetting(SettingsService.supccuKey);
			if (ksetVal.equals("1") || ksetVal.isBlank()) {
				return ResponseEntity.ok(Arrays.asList(Map.of("id", 3, "name", "Bank User")));
			}
		}
		if (auth.canFromUserTable("7")) {
			return ResponseEntity.ok(Arrays.asList(Map.of("id", 3, "name", "Bank User"),Map.of("id", 4, "name", "Supervisor")));
		}
		return ResponseEntity.ok(Arrays.asList());
	}

	public ResponseEntity<Map<String, Object>> resetPassByPin() {
		String password = request("password");
		String cpassword = request("cpassword");
		String pin = request("pincode");
		String username = request("username");
		if (!cpassword.equals(password))
			return Messenger.getMessenger().setMessage("Password and Confirm Passowrds do not match").error();

		Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$");
		Matcher matcher = pattern.matcher(password);

		if (!matcher.matches()) {
			return Messenger.getMessenger().setMessage(
					"Password must have at least 8 characters with at least one special character, one Upper case charcater and one number.")
					.error();
		}

		Tuple t = db.getSingleResult("select id, password from users where username= ?", Arrays.asList(username));
		if (t != null) {
			String sql = "select top 1 otp from otp_log where userid=? and expiry>=GETDATE() and otp=? and type=1 order by createdat desc";
			Tuple tt = db.getSingleResult(sql, Arrays.asList(t.get("id"), pin));
			if (tt != null) {
				DbResponse rowEffect = db.execute("update users set password='" + pe.encode(password)
						+ "', pwchangedate=format(getdate(),'yyyyMMdd') where id=" + t.get("id"));
				if (rowEffect.getErrorNumber() == 0) {
					return Messenger.getMessenger().setMessage("Password Changed Successfully").success();
				} else {
					return Messenger.getMessenger().setMessage("Pasword change unsuccessful").error();
				}
			} else {
				return Messenger.getMessenger().setMessage("Pin does not match.").error();
			}
		}
		return Messenger.getMessenger().setMessage("User Does not Exist..").error();
	}

	public ResponseEntity<Map<String, Object>> uploadUsers(MultipartFile mfile) {
		try {
			InputStream file = mfile.getInputStream();// FileInputStream(new File(fileAbspath.toString()));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			int count = 0;
			String importid = db.newIdInt();
			System.out.println("import id: "+importid);
			while (rowIterator.hasNext()) {
				// skipping the header row
				if (count == 0) {
					count++;
					rowIterator.next();
					continue;
				}
				Row row = rowIterator.next();
				Iterator<Cell> itCell = row.cellIterator();
				List<Object> args = new ArrayList<>();
				args.add(importid);
				int i = 0;
				while (itCell.hasNext()) {
					if (i == 0) {
						String cv = readCellValue(itCell.next());
						String[] cvs = cv.split("\\|");
						args.add(cvs[0]);
					} else if (i == 4) {
						args.add(pe.encode(readCellValue(itCell.next())));
					}else if (i == 8) {
						String cv = readCellValue(itCell.next());
						String[] cvs = cv.split("\\|");
						if(cvs[0].equals("SUPERVISOR")) {
							args.add("4");
						}else {
							args.add(cvs[0]);
						}
					}else {
						args.add(readCellValue(itCell.next()));
					}
					i++;
				}
//                args.add(readCellValue(row.getCell(0)).substring(0,4));
				String sql = "insert into imported_users (importid,branchid,name,post,username,password,mobile,email,amountlimit,role) values (?,?,?,?,?,?,?,?,?,?)";
				db.execute(sql, args);
			}
			workbook.close();
			String bankid = auth.getBankId();

			db.execute(
					"insert into users(bankid,branchid,name,post,username,password,mobile,email,amountlimit,permid) select '"
							+ bankid
							+ "',branchid,name,post,username,password,mobile,email,amountlimit,role from imported_users where importid='"
							+ importid + "'");
			db.execute(
					"insert into users_perms(userid,permid) select u.id,'3' from imported_users iu join users u on iu.username=u.username where iu.importid='"
							+ importid + "'");
			db.execute(
					"insert into users_perms(userid,permid) select u.id,'4' from imported_users iu join users u on iu.username=u.username where iu.importid='"
							+ importid + "' and iu.role=4");
			return Messenger.getMessenger().success();
		} catch (IOException el) {
			el.printStackTrace();
			return Messenger.getMessenger().error();
		}
	}

	private String readCellValue(Cell c) {
		return formatter.formatCellValue(c);
	}

	public XSSFWorkbook downloadImportFile() {
		List<Tuple> lt = db.getResultList("select id,name from branches where bankid=?",
				Arrays.asList(auth.getBankId()));
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet usheet = wb.createSheet("Users");
		XSSFSheet rfsheet = wb.createSheet("Branches");
		int r = 0;
		if (lt.size() > 0) {
			for (Tuple t : lt) {
				Row ro = rfsheet.createRow(r);
				ro.createCell(0).setCellValue(t.get("id") + "|" + t.get("name"));
				r++;
			}
		}
		// create name for Categories list constraint
		XSSFName namedRange = wb.createName();
		namedRange.setNameName("listbranches");
		String reference = "Branches!$A$1:$A$" + lt.size();
		namedRange.setRefersToFormula(reference);

		Row hr = usheet.createRow(0);
		hr.createCell(0).setCellValue("Branch");
		hr.createCell(1).setCellValue("Name");
		hr.createCell(2).setCellValue("Post");
		hr.createCell(3).setCellValue("Username");
		hr.createCell(4).setCellValue("Password");
		hr.createCell(5).setCellValue("Mobile");
		hr.createCell(6).setCellValue("Email");
		hr.createCell(7).setCellValue("Amount Limit");
		hr.createCell(8).setCellValue("Role");
		
		usheet.setActiveCell(new CellAddress("A1"));
		
		DataValidationHelper dvHelper = usheet.getDataValidationHelper();
		DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("listbranches");
		CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 0, 0);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
		
		String[] list = new String[] {"3|Normal","4|Supervisor"};
		DataValidationConstraint dvConstraintr = dvHelper.createExplicitListConstraint(list);
		CellRangeAddressList addressListr = new CellRangeAddressList(1, 1000, 8, 8);
		DataValidation validationr = dvHelper.createValidation(dvConstraintr, addressListr);
		
		usheet.addValidationData(validation);
		usheet.addValidationData(validationr);
		wb.setSheetHidden(1, true);
		return wb;
	}
}

class Menu{
	public String id;
	public String name;
	public String icon;
	public String link;
	public List<Menu> childs = new ArrayList<>();
	public Menu(String id,String name,String icon) {
		this.id=id;
		this.name=name;
		this.icon=icon;
	}
	public Menu(String id,String name,String icon,String link) {
		this.id=id;
		this.name=name;
		this.icon=icon;
		this.link = link;
	}
}
