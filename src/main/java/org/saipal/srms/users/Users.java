package org.saipal.srms.users;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.srms.parser.RequestParser;

public class Users {
	
	public String id;
	public String name;
	public String username;
	public String password;
	public String orgid;
	public String sectionid;
	public String approved;
	public String disabled;

    
	public void loadData(RequestParser doc) {
		for (Field f : this.getClass().getFields()) {
			String fname = f.getName();
			try {
				f.set(this, doc.getElementById(fname).value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<String> searchables() {
		return Arrays.asList("name", "username", "orgid");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("username", "required");
		rules.put("name", "required");
		rules.put("password", "required");
		rules.put("orgid", "required");
		rules.put("sectionid", "required");
		rules.put("approved", "required");
		rules.put("disabled", "required");
		return rules;
	}

}
