package org.saipal.srms.settings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SettingsService extends AutoService{
	
	@Autowired
	Authenticated auth;
	
	public static String otpKey = "otpmedium";
	public static String supccuKey = "supccu";
	
	
	public ResponseEntity<Map<String, Object>> updateSettings() {
		if(!auth.hasPermission("bankhq")) {
			return Messenger.getMessenger().setMessage("No permission to access the resoruce").error();
		}
		String otpValue = request(otpKey);
		String supccuValue = request(supccuKey);
		db.execute("delete from settings where bankid=?",Arrays.asList(auth.getBankId()));
		db.execute("insert into settings (skey,svalue,bankid) values(?,?,?)",Arrays.asList(otpKey,otpValue,auth.getBankId()));
		db.execute("insert into settings (skey,svalue,bankid) values(?,?,?)",Arrays.asList(supccuKey,supccuValue,auth.getBankId()));
		return Messenger.getMessenger().success();
	}
	
	public ResponseEntity<Map<String, Object>> getAllSettings() {
		List<Map<String, Object>> dta = db.getResultListMap("select skey,svalue from settings where bankid=?",Arrays.asList(auth.getBankId()));
		return Messenger.getMessenger().setData(dta).success();
	}
	public String getSetting(String key) {
		Tuple dta = db.getSingleResult("select svalue from settings where skey=? and bankid=?",Arrays.asList(key,auth.getBankId()));
		if(dta!=null) {
			return dta.get("svalue")+"";
		}
		return "";
	}
	
	public String getSetting(String key,String bankid) {
		Tuple dta = db.getSingleResult("select svalue from settings where skey=? and bankid=?",Arrays.asList(key,bankid));
		if(dta!=null) {
			return dta.get("svalue")+"";
		}
		return "";
	}
}
