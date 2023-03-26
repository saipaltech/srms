package org.saipal.srms.auth;

import java.util.Arrays;

import org.saipal.srms.util.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.persistence.Tuple;


@Component
public class Authenticated {
	
	@Autowired
	DB db;
	
	@Autowired
	ApplicationContext context;

	public Authrepo getAuthRequest() {
		return context.getBean(Authrepo.class);
	}
	
	public String getToken() {
		return getAuthRequest().token;
	}

	public void setToken(String token) {
		getAuthRequest().token = token;
	}

	public String getUserId() {
		return getAuthRequest().userId;
	}

	public void setUserId(String userId) {
		getAuthRequest().userId = userId;
	}

	public String getBankId() {
		return getAuthRequest().bankId;
	}

	public void setBankId(String bankId) {
		getAuthRequest().bankId = bankId;
	}

	public void setBranchId(String branchId) {
		getAuthRequest().branchId = branchId;
	}

	public String getBranchId() {
		return getAuthRequest().branchId;
	}

	public void setExtraInfo(String key, Object value) {
		getAuthRequest().extraInfo.put(key, value);
	}

	public Object getExtraInfo(String key) {
		return getAuthRequest().extraInfo.get(key);
	}
	public void setLang(String value) {
		getAuthRequest().extraInfo.put("lang", value);
	}

	public String getLang() {
		Object lang = getAuthRequest().extraInfo.get("lang");
		return (lang==null)?"Np":lang+"";
	}
	
	public void initSession() {
		String sql = "select bankid,branchid from users where id=?";
		Tuple t = db.getSingleResult(sql,Arrays.asList(getUserId()));
		setBankId(t.get("bankid")+"");
		setBranchId(t.get("branchid")+"");
	}
}
