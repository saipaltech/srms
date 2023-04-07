package org.saipal.srms.sms;

import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.util.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EasySmsGateway {

	private String url = "http://app.easy.com.np/easyApi";
	@Value("${sms.username}")
	String username ;
	@Value("${sms.password}")
	String password ;

	public JSONObject sendSms(String number, String message) throws JSONException {
		// added by pankaj 06/17/2022
		if (number.length() > 10) {
			if (number.startsWith("+")) {
				number = number.replace("+", "");
			}
			if (number.startsWith("977") && number.length() > 10) {
				number = number.substring(3);
			}
		}
		// end

		if (isNumberValid(number)) {
			HttpRequest req = new HttpRequest();
			req.setHeader("Content-Type", "");
			req.setParam("key", "EASY581eecf125e398.22302295");
			req.setParam("source", "none");
			req.setParam("destination", "977"+number);
			req.setParam("type", "1");
			req.setParam("message", message);
			return req.get(url);
		}
		return new JSONObject(Map.of("status_code", 500, "message", "Invalid Mobile Number"));
	}
	
	private boolean isNumberValid(String number) {
		if(Pattern.matches("[0-9]{10}", number)) {
			return true;
		}
		return false;
	}
}
