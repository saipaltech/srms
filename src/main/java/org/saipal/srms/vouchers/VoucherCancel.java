package org.saipal.srms.vouchers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.srms.parser.RequestParser;

public class VoucherCancel {
	public String sutralgid;
	public String sutraccid;
	public String banklgid;
	public String bankccid;
	public String sutraamount;
	public String bankamount;
	public String sksno;
	public String bksno;
	public String remarks;
	
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
		return Arrays.asList("sksno","bksno","ba.namenp");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("sksno", "required");
		rules.put("bksno", "required");
		rules.put("remarks", "required");
		rules.put("bankamount", "required");
		rules.put("sutraamount", "required");
		return rules;
	}
}
