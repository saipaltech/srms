package org.saipal.srms.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.util.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IrdPanSearchService {
	
	@Value("${ird.panapi.url}")
	String baseUrl;
	
	@Value("${ird.panapi.user}")
	String user;
	
	@Value("${ird.panapi.pass}")
	String pass;
	
	public Map<String, Object> tokenSource = new HashMap<>();
	//private Environment env = ApplicationContextProvider.getBean(Environment.class);
	//public String baseUrl = env.getProperty("ird.panapi.url");

	public String getToken() throws JSONException {
		if (tokenSource.size() > 0) {
			long timeStamp = (long) tokenSource.get("time");
			long expiry = timeStamp + 85000;
			if (System.currentTimeMillis() > expiry) {
				String token = fetchToken();
				if (token != null) {
					storeToken(token);
					return token;
				}
				return null;
			} else {
				return tokenSource.get("token") + "";
			}
		} else {
			String token = fetchToken();
			if (token != null) {
				storeToken(token);
				return token;
			}
			return null;
		}
	}

	public String fetchToken() throws JSONException {
		String url = baseUrl + "/api/user/login";
		JSONObject reqObject = new JSONObject();
		reqObject.put("Username", user);
		reqObject.put("Password", pass);
		HttpRequest request = new HttpRequest();
		JSONObject response = request.setParam(reqObject.toString()).post(url);
		if (response.getInt("status_code") == 200) {
			JSONObject resp = response.getJSONObject("data");
			String token = resp.getJSONObject("data").getString("token");
			return token;
		}
		return null;
	}

	public JSONArray getData(String panid) throws JSONException {
		String url = baseUrl + "/api/pan/getpandetail";
		String token = getToken();
		HttpRequest request = new HttpRequest();
		//panid=nep2EngNum(panid);
		JSONObject response = request.setHeader("Authorization", "Bearer " + token).get(url + "?pan=" + panid);
		if (response.getInt("status_code") == 200) {
			JSONObject resp = response.getJSONObject("data");
			JSONArray data = resp.getJSONArray("data");
			if (data.length() > 1) {
				JSONArray finalData = new JSONArray();
				Map<String, Integer> filter = new HashMap<>();
				for (int i = 0; i < data.length(); i++) {
					filter.put(data.getJSONObject(i).getString("taxpayer"), i);
				}
				// iterate over map
				Collection<Integer> indexes = filter.values();
				for (int ind : indexes) {
					finalData.put(data.get(ind));
				}
				return finalData;
			}
			return data;
		}
		return null;
	}

	public void storeToken(String token) {
		tokenSource.put("token", token);
		tokenSource.put("time", System.currentTimeMillis());
	}
}
