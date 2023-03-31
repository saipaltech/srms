package org.saipal.srms.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.srms.parser.RequestParser;
import org.saipal.srms.util.DB;
import org.saipal.srms.util.JwtHelper;
import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
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

	public ResponseEntity<Map<String,Object>> login() {
		String username = doc.getElementById("username").value;
		String password = doc.getElementById("password").value;
		String sql = "select id,username,password,name from users where username=?";
		Tuple t = db.getSingleResult(sql,Arrays.asList(username));
		if(t!=null) {
			if(pwdEncoder.matches(password, t.get("password")+"")) {
				String token = jwtHelper.createToken(t.get("id")+"");
				Map<String,String> data = new HashMap<>();
				data.put("token", token);
				data.put("name", t.get("name")+"");
				data.put("username",t.get("username")+"");
				data.put("bank", "SYSTEM");
				data.put("branch", "SYSTEM");
				return Messenger.getMessenger().setData(data).success();
			}
		}
		return Messenger.getMessenger().setMessage("Invalid username or password, Please try again.").error();
	}
	
	public Tuple geUserInfo(String userId) {
		String sql = "select id,name,email from xcs_users where id=?";
		return db.getSingleResult(sql,Arrays.asList(userId));
	}
}
