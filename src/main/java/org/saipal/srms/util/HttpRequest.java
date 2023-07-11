package org.saipal.srms.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class HttpRequest {
	private Map<String, String> header = new HashMap<>();
	private Map<String, String> params = new HashMap<>();
	private String stringParam = "";
	private String[] errorflds = {"message","status_code"};
	
	private CloseableHttpClient getHttpClient() {
		return HttpClients.custom()
				.setConnectionManagerShared(true)
		        .build();
	}

	private void setDefaultHeaders() {
		if (!header.containsKey("Accept")) {
			header.put("Accept", "application/json");
		}
		if (!header.containsKey("Content-Type")) {
			header.put("Content-Type", "application/json");
		}
		if (!header.containsKey("Content-Encoding")) {
			header.put("Content-Encoding", "UTF-8");
		}
		
	}

	public HttpRequest removeHeader(String key) {
		this.header.remove(key);
		return this;
	}

	public HttpRequest setHeader(String key, String value) {
		this.header.put(key, value);
		return this;
	}

	public HttpRequest setParam(String key, String value) {
		this.params.put(key, value);
		return this;
	}
	
	public HttpRequest setParam(String param) {
		this.stringParam = param;
		return this;
	}
	
	public HttpRequest setParamAll(Map<String,String> data) {
		this.params = data;
		return this;
	}

	public JSONObject get(String url){
		setDefaultHeaders();
		if (params.size() > 0) {
			try {
				URIBuilder builder = new URIBuilder(url);
				for (String key : params.keySet()) {
					builder.setParameter(key, params.get(key));
				}
				url = builder.build().toString();
			} catch (Exception e) {
				ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
				return new JSONObject(erm, errorflds);
			}
		}
		HttpGet http = new HttpGet(url);
		if (header.size() > 0) {
			for (String key : header.keySet()) {
				http.setHeader(key, header.get(key));
			}
		}
		
		try (CloseableHttpResponse response = getHttpClient().execute(http)) {
			String resp;
			try {
				resp = EntityUtils.toString(response.getEntity());
			} catch (ParseException e) {
				ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
				return new JSONObject(erm, errorflds);
			}
			JSONObject data = new JSONObject();
			if (resp.startsWith("{")) {
				data.put("data", new JSONObject(resp));
			} else if (resp.startsWith("[")) {
				data.put("data", new JSONArray(resp));
			} else {
				data.put("data", resp);
			}
			data.put("status_code", response.getCode());
			return data;
		} catch (ClientProtocolException e) {
			ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
			return new JSONObject(erm, errorflds);
		} catch (IOException e) {
			ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
			return new JSONObject(erm, errorflds);
		} catch (JSONException e) {
			ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
			return new JSONObject(erm, errorflds);
		}
	}

	public JSONObject post(String url){
		setDefaultHeaders();
		HttpPost http = new HttpPost(url);
		if (header.size() > 0) {
			for (String key : header.keySet()) {
				http.setHeader(key, header.get(key));
			}
		}
		ErrorMessage ermj = null;
		if (!stringParam.isBlank()) {
			http.setEntity(new StringEntity(stringParam,StandardCharsets.UTF_8));
		} else if (params.size() > 0) {
			if (header.get("Content-Type") == "application/json") {
				try {
					http.setEntity(new StringEntity((new JSONObject(params)).toString(),StandardCharsets.UTF_8));
				} catch (JSONException e) {
					ermj = new ErrorMessage(e.getMessage(),400);
				}
			} else {
				List<NameValuePair> listOfParams = new ArrayList<>();
				for (String key : params.keySet()) {
					listOfParams.add(new BasicNameValuePair(key, params.get(key)));
				}
				http.setEntity(new UrlEncodedFormEntity(listOfParams,StandardCharsets.UTF_8));
			}
		}
		if(ermj!=null) {
			return new JSONObject(ermj, errorflds);
		}
		try (CloseableHttpResponse response = getHttpClient().execute(http)) {
			String resp;
			try {
				resp = EntityUtils.toString(response.getEntity()).trim();
			} catch (ParseException e) {
				ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
				return new JSONObject(erm, errorflds);
			}
			//System.out.println("rsp: " + resp);
			JSONObject data = new JSONObject();
			if (resp.startsWith("{")) {
				data.put("data", new JSONObject(resp));
			} else if (resp.startsWith("[")) {
				data.put("data", new JSONArray(resp));
			} else {
				data.put("data", resp);
			}
			data.put("status_code", response.getCode());
			return data;
		} catch (ClientProtocolException e) {
			ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
			return new JSONObject(erm, errorflds);
		} catch (IOException e) {
			ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
			return new JSONObject(erm, errorflds);
		} catch (JSONException e) {
			ErrorMessage erm = new ErrorMessage(e.getMessage(),400);
			return new JSONObject(erm, errorflds);
		}
	}
}

final class ErrorMessage {
	public String message;
	public int status_code;
	public ErrorMessage(String msg,int code) {
		message = msg;
		status_code = code;
	}
}
