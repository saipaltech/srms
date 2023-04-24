package org.saipal.srms.util;

import java.util.Arrays;

import javax.persistence.Tuple;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.auth.Authenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiManager {
	@Value("${sutra.url}")
	String url;

	@Value("${sutra.username}")
	String username;

	@Value("${sutra.password}")
	String password;

	public String token="";

	@Autowired
	JwtHelper jwt;
	
	@Autowired
	DB db;
	
	@Autowired
	Authenticated auth;

	public String getToken() {
//		if (!token.isBlank()) {
//			if (!jwt.isExpired(token)) {
//				return token;
//			}
//		}
		try {
			HttpRequest req = new HttpRequest();
			JSONObject response = req.setParam("username", username).setParam("password", password).setHeader("Content-Type","application/x-www-form-urlencoded")
					.post(url + "/get-auth-token");
			if (response.getInt("status_code") == 200) {
				this.token = response.getJSONObject("data").getString("token");
				return token;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return "";
	}

	public JSONObject getBanks() {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/banks");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject localLevels(String bankCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/local-levels?bankcode="+bankCode);
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from cllg where bankid="+auth.getBankId());
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into cllg (bankid,code,name) values (?,?,?)",Arrays.asList(auth.getBankId(),d.getString("code"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject costCentres(String llgCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/costcenter?llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from ccostcnt where bankid="+auth.getBankId());
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into ccostcnt (bankid,llgcode,code,name) values (?,?,?,?)",Arrays.asList(auth.getBankId(),llgCode,d.getString("code"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject bankAccounts(String bankCode,String llgCode) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/bankac?bankcode="+bankCode+"&llgcode="+llgCode);
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from cbankac where bankid="+auth.getBankId());
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into cbankac (bankid,llgcode,acno,name) values (?,?,?,?)",Arrays.asList(auth.getBankId(),llgCode,d.getString("acno"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	public JSONObject revenueCodes() {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/revenuelist");
			if (response.getInt("status_code") == 200) {
				//insert to the cache table
				db.execute("delete from crevenue where 1=1");
				JSONObject data = response.getJSONObject("data");
				if(data.getInt("status")==1) {
					JSONArray llgList = data.getJSONArray("data");
					if(llgList.length()>0) {
						for(int i=0;i<llgList.length();i++) {
							JSONObject d = llgList.getJSONObject(i);
							db.execute("insert into crevenue (code,name) values (?,?)",Arrays.asList(d.getString("code"),d.getString("name")));
						}
					}
				}
				return data;
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Calls SuTra server to post taxpayer voucher details
	 * */
	public JSONObject sendDataToSutra(Tuple tpv, String revs) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.setParam("id",tpv.get("id")+"")
					.setParam("date",tpv.get("date")+"")
					.setParam("voucherno",tpv.get("voucherno")+"")
					.setParam("taxpayername",tpv.get("taxpayername")+"")
					.setParam("taxpayerpan",tpv.get("taxpayerpan")+"")
					.setParam("depositedby",tpv.get("depositedby")+"")
					.setParam("depcontact",tpv.get("depcontact")+"")
					.setParam("lgid",tpv.get("lgid")+"")
					.setParam("collectioncenterid",tpv.get("collectioncenterid")+"")
					.setParam("bankorgid",tpv.get("bankorgid")+"")
					.setParam("purpose",tpv.get("purpose")+"")
					.setParam("bankid",tpv.get("bankid")+"")
					.setParam("branchid",tpv.get("branchid")+"")
					.setParam("deposituserid",tpv.get("deposituserid")+"")
					.setParam("approved",tpv.get("approved")+"")
					.setParam("approverid",tpv.get("approverid")+"")
					.setParam("updatedon",tpv.get("updatedon")+"")
					.setParam("chequebank",tpv.get("chequebank")+"")
					.setParam("chequeamount",tpv.get("chequeamount")+"")
					.setParam("chequeno",tpv.get("chequeno")+"")
					.setParam("cstatus",tpv.get("cstatus")+"")
					.setParam("ttype",tpv.get("ttype")+"")
					.setParam("revenue",revs)
					.setParam("amountdr",tpv.get("amountdr")+"")
					.setParam("amountcr",tpv.get("amountcr")+"")
					.setParam("depositbankid", tpv.get("depositbankid") + "")
					.setParam("depositbranchid", tpv.get("depositbranchid") + "")
					.post(url + "/srms/taxpayer-voucher");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject sendDataToSutraPalikachange(String id,String llid,String lgid,String collectioncenterid,String accountno,String remarks,String userid) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.setParam("id",id)
					.setParam("llid",llid)
					.setParam("lgid",lgid)
					.setParam("collectioncenterid",collectioncenterid)
					.setParam("accountno",accountno)					
					.setParam("remarks",remarks)
					.setParam("creatorid",userid)
					.post(url + "/srms/taxpayer-voucher-update-palikachange");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	
	
	/*
	 * Calls sutra API to send the status of bank voucher deposited, 
	 * Voucher Number is used to communicate the information
	 * */
	public JSONObject updateToSutra(String transid,String bankVoucherid,String depositdate,String remarks) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.setParam("transactionid",transid)
					.setParam("depositdate",depositdate)
					.setParam("bankvoucherno",bankVoucherid)
					.setParam("remarks",remarks)
					.setParam("status","1")
					.post(url + "/srms/bankdeposit-voucher");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Calls Sutra API to get the voucher details by the payment reference number
	 * */
	public JSONObject getTransDetails(String transactionid) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/get-trans-details?transactionid="+transactionid);
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Calls Sutra API to get the cheque details by the karobarsanket and bankid
	 * */
	public JSONObject getChequeDetails(String karobarsanket) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/get-cheque-details?karobarsanket="+karobarsanket);
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Calls Sutra API to get the Tax Voucher Usage status by voucher id
	 * */
	public JSONObject getVoucherDetails(String id) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.get(url + "/srms/get-taxvoucher-detail?id="+id);
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}

	public JSONObject saveVoucherUpdates(String id, String taxpayername, String taxpayerpan, String amount,String lgid,String ccid,String acno,String voucher) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.setParam("id",id)
					.setParam("taxpayername",taxpayername)
					.setParam("taxpayerpan",taxpayerpan)
					.setParam("amount",amount)
					.setParam("voucherinfo",voucher)
					.setParam("lgid",lgid)
					.setParam("collectioncenterid",ccid)
					.setParam("bankorgid",acno)
					.post(url + "/srms/taxpayer-voucher-update-namechange");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}

	public JSONObject getPalikaResponse(String vrefid,String id) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.setParam("id",id)
					.setParam("vrefid",vrefid)
					.get(url + "/srms/get-palika-response");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}

	public JSONObject settlePalikaChange(String id,String lgid,String ccid,String bankorgid) {
		HttpRequest req = new HttpRequest();
		String tok = this.getToken();
		try {
			JSONObject response = req
					.setHeader("Authorization", "Bearer "+tok)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.setParam("id",id)
					.setParam("lgid",lgid)
					.setParam("collectioncenterid",ccid)
					.setParam("bankorgid",bankorgid)
					.post(url + "/srms/taxpayer-voucher-settle-update");
			if (response.getInt("status_code") == 200) {
				return response.getJSONObject("data");
			}
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return null;
	}
}
