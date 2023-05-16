package org.saipal.srms.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.persistence.Tuple;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.parser.RequestParser;
import org.saipal.srms.sms.F1SoftSmsGateway;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.JwtHelper;
import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

	@Autowired
	DB db;

	@Autowired
	RequestParser doc;

	@Autowired
	JwtHelper jwtHelper;

	@Autowired
	PasswordEncoder pwdEncoder;

	@Autowired
	Authenticated auth;

	@Autowired
	F1SoftSmsGateway sms;

	@Value("${sms.otpmessage}")
	private String msgFormat;
	
	@Value("${sms.pinmessage}")
	private String pinMsgFormat;

	public ResponseEntity<Map<String, Object>> login() {
		String username = doc.getElementById("username").value;
		String password = doc.getElementById("password").value;
		String sql = "select u.id,username,password,u.name,u.approved,u.disabled b.namenp as baname, bs.name as branchname,bs.dlgid from users u join bankinfo b on b.id=u.bankid join branches bs on bs.id=u.branchid where username=?";
		Tuple t = db.getSingleResult(sql, Arrays.asList(username));
		if (t != null) {
			if (!(t.get("approved") + "").equals("1")) {
				return Messenger.getMessenger().setMessage("User not Approved.").error();
			}
			if (!(t.get("disabled") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("User not Enabled.").error();
			}
			if (pwdEncoder.matches(password, t.get("password") + "")) {
				String token = jwtHelper.createToken(t.get("id") + "");
				Map<String, String> data = new HashMap<>();
				data.put("token", token);
				data.put("name", t.get("name") + "");
				data.put("username", t.get("username") + "");
				data.put("bank", t.get("baname") + "");
				data.put("branch", t.get("branchname") + "");
				data.put("dlgid", t.get("dlgid") + "");
				return Messenger.getMessenger().setData(data).success();
			}
		}
		return Messenger.getMessenger().setMessage("Invalid username or password, Please try again.").error();
	}

	public ResponseEntity<Map<String, Object>> checkUser() {
		String username = doc.getElementById("username").value;
		String password = doc.getElementById("password").value;
		String sql = "select id,username,password,mobile,approved,disabled from users where username=?";
		Tuple t = db.getSingleResult(sql, Arrays.asList(username));
		if (t != null) {
			if (!(t.get("approved") + "").equals("1")) {
				return Messenger.getMessenger().setMessage("User not Approved.").error();
			}
			if (!(t.get("disabled") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("User not Enabled.").error();
			}
			String mobile = t.get("mobile") + "";
			if (mobile.isBlank()) {
				return Messenger.getMessenger().setMessage("Please register your mobile number to login.").error();
			}
			if (pwdEncoder.matches(password, t.get("password") + "")) {
				Map<String, String> data = new HashMap<>();
				String reqid = db.newIdInt();
				data.put("reqid", reqid);
				data.put("userid", t.get("id") + "");
				Random rnd = new Random();
				 int number = rnd.nextInt(999999);
				 String otp = String.format("%06d", number);
				//String otp = "123456";
				String sq = "insert into otp_log (reqid,userid,mobileno,expiry,otp) values (?,?,?,(select DATEADD(MINUTE, 2, GETDATE())),?)";
				DbResponse resp = db.execute(sq, Arrays.asList(reqid, t.get("id"), mobile, otp));
				if (resp.getErrorNumber() == 0) {
					String message = msgFormat.replace("OTPCODE", otp);
					try {
						JSONObject ob = sms.sendSms(t.get("mobile")+"", message, reqid);
						if ( ob.getInt("status_code")==200) {
							return Messenger.getMessenger().setData(data).setMessage(
									"An OTP has been Sent to your registered mobile. Please insert the OTP below and submit")
									.success();
						}
						return Messenger.getMessenger().setMessage(ob.getString("message")).error();
					} catch ( JSONException e) {
						return Messenger.getMessenger().setMessage("Unable to send sms for OTP").error();
					}
				}
				return Messenger.getMessenger().setMessage("Unable to send sms for OTP").success();
			}
		}
		return Messenger.getMessenger().setMessage("Invalid username or password, Please try again.").error();
	}

	public Tuple geUserInfo(String userId) {
		String sql = "select id,name,email from xcs_users where id=?";
		return db.getSingleResult(sql, Arrays.asList(userId));
	}

	public ResponseEntity<Map<String, Object>> reLogin() {
		String token = jwtHelper.createToken(auth.getUserId());
		return Messenger.getMessenger().setData(Map.of("token", token)).success();
	}

	public ResponseEntity<Map<String, Object>> twoFa() {
		String otp = doc.getElementById("otp").value;
		String reqid = doc.getElementById("reqid").value;
		String userid = doc.getElementById("userid").value;
		if (otp.isBlank() || reqid.isBlank() || userid.isBlank()) {
			return Messenger.getMessenger().setMessage("Invalid Request").error();
		}
		String sql = "select top 1 otp from otp_log where reqid=? and userid=? and expiry>=GETDATE() and otp=? and type=0 order by createdat desc";
		Tuple tt = db.getSingleResult(sql, Arrays.asList(reqid, userid, otp));
		if (tt != null) {
			sql = "select u.id,username,password,u.name, u.pwchangedate, b.namenp as baname, bs.name as branchname,bs.dlgid from users u join bankinfo b on b.id=u.bankid join branches bs on bs.id=u.branchid where u.id=?";
			Tuple t = db.getSingleResult(sql, Arrays.asList(userid));
			if (t != null) {
				Map<String, String> data = new HashMap<>();
				if (needpwdchange(t.get("pwchangedate"))) {
					data.put("username", t.get("username") + "");
					return Messenger.getMessenger().setData(data).success();
				}
				String token = jwtHelper.createToken(t.get("id") + "");
				data.put("token", token);
				data.put("name", t.get("name") + "");
				data.put("username", t.get("username") + "");
				data.put("bank", t.get("baname") + "");
				data.put("branch", t.get("branchname") + "");
				data.put("dlgid", t.get("dlgid") + "");
				return Messenger.getMessenger().setData(data).success();
			}
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
	}

	public ResponseEntity<Map<String, Object>> apiLogin() {
		String username = doc.getElementById("username").value;
		String password = doc.getElementById("password").value;
		String sql = "select u.id,username,password,u.name from users u where username=?";
		Tuple t = db.getSingleResult(sql, Arrays.asList(username));
		if (t != null) {
			if (pwdEncoder.matches(password, t.get("password") + "")) {
				String token = jwtHelper.createTokenApi(t.get("id") + "");
				Map<String, String> data = new HashMap<>();
				data.put("token", token);
				data.put("name", t.get("name") + "");
				return Messenger.getMessenger().setData(data).success();
			}
		}
		return Messenger.getMessenger().setMessage("Invalid username or password, Please try again.").error();
	}

	public boolean needpwdchange(Object pwdchangedate) {
		if (pwdchangedate == null) {
			return true;
		}
		if ((pwdchangedate + "").isBlank()) {
			return true;
		}
		// check date logic

		return false;
	}

	public ResponseEntity<Map<String, Object>> getPincode() {
		String username = doc.getElementById("username").value;
		if (username.isBlank()) {
			return Messenger.getMessenger().setMessage("Please provide your username.").error();
		}
		String sql = "select id,username,password,mobile,email,approved,disabled from users where username=?";
		Tuple t = db.getSingleResult(sql, Arrays.asList(username));
		if (t != null) {
			if (!(t.get("approved") + "").equals("1")) {
				return Messenger.getMessenger().setMessage("User not Approved.").error();
			}
			if (!(t.get("disabled") + "").equals("0")) {
				return Messenger.getMessenger().setMessage("User not Enabled.").error();
			}
			String mobile = t.get("mobile") + "";
			if (mobile.isBlank()) {
				return Messenger.getMessenger().setMessage("Please register your mobile number.").error();
			}
			String email = t.get("email") + "";
			if (email.isBlank()) {
				return Messenger.getMessenger().setMessage("Please register your E-Mail.").error();
			}
			 Random rnd = new Random();
			 int number = rnd.nextInt(999999);
			 String otp = String.format("%06d", number);
			String reqid = db.newIdInt();
			String sq = "insert into otp_log (reqid,userid,mobileno,expiry,otp,type) values (?,?,?,(select DATEADD(MINUTE, 2, GETDATE())),?,?)";
			DbResponse resp = db.execute(sq, Arrays.asList(reqid, t.get("id"), mobile, otp,1));
			if (resp.getErrorNumber() == 0) {
				String message = pinMsgFormat.replace("OTPCODE", otp);
				try {
					 JSONObject ob = sms.sendSms(t.get("mobile")+"", message, reqid);
					if (ob.getInt("status_code")==200) {
						return Messenger.getMessenger().setMessage(
								"A Pin Code has been Sent to your registered mobile. Please insert the Pin to reset password.")
								.success();
					}
					return Messenger.getMessenger().setMessage( ob.getString("message")).error();
				} catch ( JSONException e) {
					//e.printStackTrace();
					return Messenger.getMessenger().setMessage("Unable to send sms for OTP").error();
				}
			}
			return Messenger.getMessenger().setMessage("No Such user exists.").error();
		}else {
			return Messenger.getMessenger().setMessage("No Such user exists.").error();
		}
	}
}
