package org.saipal.srms.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiManager {
	@Value("${sutra.url}")
	String url;

	@Value("${sutra.username}")
	String username;

	@Value("${sutra.password}")
	String password;

	public String token="";

	@Autowired
	JwtHelper jwt;

	public String getToken() {
//		if (!token.isBlank()) {
//			if (!jwt.isExpired(token)) {
//				return token;
//			}
//		}
		try {
			HttpRequest req = new HttpRequest();
			JSONObject response = req.setParam("username", username).setParam("password", password).setHeader("Content-Type","application/x-www-form-urlencoded")
					.post(url + "/get-auth-token");
			if (response.getInt("status_code") == 200) {
				this.token = response.getJSONObject("data").getString("token");
				return token;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return "";
	}

	public JSONObject getBanks() {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/banks");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject localLevels(String bankCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/local-levels?bankcode="+bankCode);
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONArray costCentres(String llgCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/costcenter?llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				return response.getJSONArray("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONArray bankAccounts(String bankCode,String llgCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/bankac?bankcode="+bankCode+"&llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				return response.getJSONArray("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	public JSONArray revenueCodes(String llgCode) { 
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/revenuelist?llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				return response.getJSONArray("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONArray getVoucherDetails(String voucherno) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/revenuelist?voucherno="+voucherno);
			if (response.getInt("status_code") == 200) {
				return response.getJSONArray("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
}
