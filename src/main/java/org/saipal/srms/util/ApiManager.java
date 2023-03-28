package org.saipal.srms.util;

import java.util.Arrays;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.auth.Authenticated;
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
	
	@Autowired
	DB db;
	
	@Autowired
	Authenticated auth;

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
				//insert to the cache table
				db.execute("delete from cllg where bankid="+auth.getBankId());
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into cllg (bankid,code,name) values (?,?,?)",Arrays.asList(auth.getBankId(),d.getString("code"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject costCentres(String llgCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/costcenter?llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from ccostcnt where bankid="+auth.getBankId());
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into ccostcnt (bankid,llgcode,code,name) values (?,?,?,?)",Arrays.asList(auth.getBankId(),llgCode,d.getString("code"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject bankAccounts(String bankCode,String llgCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/bankac?bankcode="+bankCode+"&llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from cbankac where bankid="+auth.getBankId());
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into cbankac (bankid,llgcode,acno,name) values (?,?,?,?)",Arrays.asList(auth.getBankId(),llgCode,d.getString("acno"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	public JSONObject revenueCodes() {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/revenuelist");
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from crevenue where 1=1");
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into crevenue (code,name) values (?,?)",Arrays.asList(d.getString("code"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject getVoucherDetails(String voucherno) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/revenuelist?voucherno="+voucherno);
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
}
