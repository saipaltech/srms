package org.saipal.srms.vouchers;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.srms.parser.RequestParser;

public class TaxPayerVoucher {
	
	public String id;
	public String date;
	public String voucherno;
	public String taxpayername;
	public String taxpayerpan;
	public String depositedby;
	public String depcontact;
	public String lgid;
	public String llgname;
	public String collectioncenterid;
	public String costcentername;
	public String bankorgid;
	public String purpose;
	public String amount;
	public String chequebank;
	public String chequeno;
	public String chequeamount;
	public String ttype;
	public String chequetype;
	public String depositbankid;
	public String depositbranchid;
	public String directdeposit;
	

    
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
		return Arrays.asList("voucherno", "taxpayername","chequeno","karobarsanket","amountcr","chequeamount");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("date", "required");
//		rules.put("voucherno", "required");
		rules.put("taxpayername", "required");
		rules.put("depositedby", "required");
		rules.put("collectioncenterid", "required");
		return rules;
	}

}
