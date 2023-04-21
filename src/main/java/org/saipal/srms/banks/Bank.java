package org.saipal.srms.banks;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.srms.parser.RequestParser;

public class Bank {
	
	public String id;
	public String name;
	public String code;
	public String bankid;
	public String district;
	public String maddress;
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
		return Arrays.asList("name", "code", "id");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("bankid", "required");
		rules.put("approved", "required");
		rules.put("disabled", "required");
		return rules;
	}

}
