package org.saipal.srms.sms;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.util.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class F1SoftSmsGateway {

	@Value("${sms.f1soft.url}")
	private String url;
	private String ntcRegex = "[9][8][4-6]{1}[0-9]{7}";
	private String ntcRegex1 = "[9][7][4-6]{1}[0-9]{7}";
	private String ncellRegex = "[9][8][0-2]{1}[0-9]{7}";
	
	@Value("${sms.f1soft.ntc.username}")
	String f1NtcUsername ;
	
	@Value("${sms.f1soft.ntc.password}")
	String f1NtcPass ;
	
	@Value("${sms.f1soft.ncell.username}")
	String f1NcellUsername ;
	
	@Value("${sms.f1soft.ncell.password}")
	String f1NcellPass ;

	public JSONObject sendSms(String number, String message,String id) throws JSONException {
		//added by pankaj 06/17/2022
		if (number.length() > 10) {
			if (number.startsWith("+")) {
				number = number.replace("+", "");
			}
			if (number.startsWith("977") && number.length()>10) {
				number = number.substring(3);
			}
		}
		// end 
		
		if(isNumberValid(number)) {
			String[] credentials = getCredentials(number);
			if(credentials!=null) {
				String nonce = UUID.randomUUID().toString();
				try {
					JSONObject payLoad = new JSONObject();
					payLoad.put("mobileNumber", number);
					payLoad.put("message", message);
					payLoad.put("uniqueId",id);
					payLoad.put("type","otp");
					String requestPayload = payLoad.toString();
					String authHeader = getHash(credentials[0],credentials[1],nonce,requestPayload);
					if(authHeader!=null) {
						HttpRequest req = new HttpRequest();
						req.setHeader("Content-Type", "application/json");
						req.setHeader("Authorization",authHeader);
						req.setHeader("Authorization-Type", "HMAC");
						req.setParam(requestPayload);
						return req.post(url);
					}
				} catch (JSONException e) {
					return new JSONObject(Map.of("status_code",500,"message","Invalid Inputs"));
				}
			}
		}
		return new JSONObject(Map.of("status_code",500,"message","Invalid Mobile Number"));
	}

	/*public JSONObject sendOtp(String number) {
		return null;
	}*/

	private String getHash(String user, String pass, String nonce, String payload){
		String delimiter = " ";
		String rawPayload = delimiter + user + delimiter + nonce + delimiter + payload + delimiter;
		try {
			Mac hasher = Mac.getInstance("HmacSHA512");
			hasher.init(new SecretKeySpec(pass.getBytes(), "HmacSHA512"));
			byte[] hash = hasher.doFinal(rawPayload.getBytes());
			String signature = DatatypeConverter.printBase64Binary(hash);
			return "HmacSHA512" + " " + user + ":" + nonce + ":" + signature;
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			return null;
		}
	}

	private String[] getCredentials(String number) {
		String[] credentials = new String[2];
		if (Pattern.matches(ntcRegex, number) || Pattern.matches(ntcRegex1, number)) {
			credentials[0] = f1NtcUsername;
			credentials[1] = f1NtcPass;
			return credentials;
		} else if (Pattern.matches(ncellRegex, number)) {
			credentials[0] =f1NcellUsername ;
			credentials[1] = f1NcellPass;
			return credentials;
		}
		return null;
	}
	
	private boolean isNumberValid(String number) {
		if (Pattern.matches(ntcRegex, number) || Pattern.matches(ntcRegex1, number) || Pattern.matches(ncellRegex, number)) {
			return true;
		}
		return false;
	}
}
