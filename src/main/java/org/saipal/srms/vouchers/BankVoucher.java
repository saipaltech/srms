package org.saipal.srms.vouchers;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.srms.parser.RequestParser;

public class BankVoucher {
	
	public String id;
	public String transactionid;
	public String office;
	public String voucherdate;
	public String bankacname;
	public String bankacno;
	public String depositdate;
	public String bankvoucherno;
	public String remarks;
	public String creatorid;
	public String approverid;
	public String status;
	public String approved;
	public String tasklog;
	

    
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
		return Arrays.asList("transactionid");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("transactionid", "required");
		rules.put("depositdate", "required");
		rules.put("remarks", "required");
		rules.put("bankvoucherno", "required");
		return rules;
	}

}
