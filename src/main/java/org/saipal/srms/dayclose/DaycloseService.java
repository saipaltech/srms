package org.saipal.srms.dayclose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.service.IrdPanSearchService;
import org.saipal.srms.util.ApiManager;
import org.saipal.srms.util.DbResponse;
import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DaycloseService extends AutoService {
	@Autowired
	Authenticated auth;

	@Autowired
	ApiManager api;

	@Autowired
	IrdPanSearchService pan;
	public ResponseEntity<Map<String, Object>> getdayclose() {
		String date=request("date");
		String lgid=request("lgid");
		String acno=request("acno");
		System.out.println(lgid);
		String cond="";
		if(!lgid.isBlank()) {
			cond+=" and t.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond+=" and t.accountno='"+acno+"'";
		}
		
		String cond1="";
		if(!lgid.isBlank()) {
			cond1+=" and t.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond1+=" and t.bankorgid='"+acno+"'";
		}
		
		String sql = "select *,(amountcr-amountdr) as balance from (select accountno,accountname,accountnumber,palika,lgid,sum(amountcr) as amountcr,sum(amountdr) as amountdr from ("
				+" select  cast(t.accountno as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.accountno left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.accountno and dc.dateint=t.dateint   where  dc.id is null and t.dateint=format(getdate(),'yyyyMMdd')  and  t.bankid=? and t.ttype=1 "+cond 
				+" union"
				+" select  cast(t.accountno as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.accountno  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.accountno and dc.dateint=t.dateint   where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.ttype=1 "+cond 
				+ " union"
				+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amount as amountcr,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.depositdateint   where  dc.id is null and  t.depositdateint=format(getdate(),'yyyyMMdd') and  t.bankid=?  "+cond1
				+" ) a group by accountno,accountname,accountnumber,palika,lgid) b ";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(auth.getBankId(),auth.getBankId(),auth.getBankId()));
		if(admlvl.isEmpty()) {
			return Messenger.getMessenger().setMessage("No transaction found").error();
		}else {
			List<Map<String, Object>> list = new ArrayList<>();
			if (!admlvl.isEmpty()) {
				for (Tuple t : admlvl) {
					Map<String, Object> mapadmlvl = new HashMap<>();
					mapadmlvl.put("accountno", t.get("accountno"));
					mapadmlvl.put("lgid", t.get("lgid"));
					mapadmlvl.put("amountcr", t.get("amountcr"));
					mapadmlvl.put("amountdr", t.get("amountdr"));
					mapadmlvl.put("accountname", t.get("accountname"));
					mapadmlvl.put("accountnumber", t.get("accountnumber"));
					mapadmlvl.put("palika", t.get("palika"));
					mapadmlvl.put("balance", t.get("balance"));
					
					list.add(mapadmlvl);
				}
		}
			return Messenger.getMessenger().setData(list).success();
	}
//		return null;
}

	@Transactional
	public ResponseEntity<Map<String, Object>> submitdayclose() throws JSONException {
		// TODO Auto-generated method stub
		String date=request("date");
		String cbid=request("corebankid");
		String lgid=request("lgid");
		String acno=request("acno");
		String items=request("selection");
		if (!items.startsWith("[")) {
			items = "[" + items + "]";
		}
		String corebank=request("corebank");
		JSONObject cb = new JSONObject(corebank);
		System.out.println(cb);
		JSONArray jarr = new JSONArray(items);
		
		if (jarr.length() > 0) {
			for (int i = 0; i < jarr.length(); i++) {
				String[] parts = jarr.get(i).toString().split("\\|\\|");
				String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
				Map<String,Object> t = db.getSingleResultMap(sq,Arrays.asList(parts[0],parts[1]));
				if(t.get("cid").toString().equals("0")) {
					String id=db.newIdInt();
//					System.out.println(cb.get(parts[1]));
					String sql = "insert into dayclose(id,lgid,bankorgid,accountno,accountname,amountdr,amountcr,bankid,branchid,creatorid,corebankid,dateint) values (?,?,?,?,?,?,?,?,?,?,?,format(getdate(),'yyyyMMdd')) ";
					DbResponse rowEffect = db.execute(sql,
							Arrays.asList(id,parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],auth.getBankId(),auth.getBranchId(),auth.getUserId(),cb.get(parts[1])));
					String sql1="select * from taxvouchers where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> admlvl = db.getResultList(sql1, Arrays.asList(parts[0],parts[1]));
					if (!admlvl.isEmpty()) {
						for (Tuple tt : admlvl) {
							String sql2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,amount,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
							db.execute(sql2,
									Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("accountno"),tt.get("amount"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("changeReqestDate"),tt.get("amountdr"),tt.get("amountcr")));
						}
				}
					
				}

			}
			return Messenger.getMessenger().success();
		}else {
			return Messenger.getMessenger().error();
		}
//		String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
//		Map<String,Object> t = db.getSingleResultMap(sq,Arrays.asList(lgid,acno));
////		System.out.println(t.get("cid"));
//		if(t.get("cid").toString().equals("1")) {
//			return Messenger.getMessenger().setMessage("Day close has been done already").error();
//		}
//		if(date.isBlank() ||lgid.isBlank() || acno.isBlank()) {
//			return Messenger.getMessenger().setMessage("Required field is not supplied.").error();
//		}
//		String sql = "insert into dayclose(id,lgid,accountno,creatorid,corebankid,dateint) values (?,?,?,?,?,format(getdate(),'yyyyMMdd')) ";
//		DbResponse rowEffect = db.execute(sql,
//				Arrays.asList(id,lgid,acno,auth.getUserId(),cbid));
//		if (rowEffect.getErrorNumber() == 0) {
//			String sql1="select id, karobarsanket,taxpayername,amount from taxvouchers where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
//			List<Tuple> admlvl = db.getResultList(sql1, Arrays.asList(lgid, acno));
//			System.out.println(admlvl);
//			if (!admlvl.isEmpty()) {
//				for (Tuple tt : admlvl) {
//					String sql2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint) values (?,?,?,format(getdate(),'yyyyMMdd')) ";
//					db.execute(sql2,
//							Arrays.asList(id,tt.get("id"),tt.get("karobarsanket")));
//				}
//		}
//			return Messenger.getMessenger().success();
//		}else {
//			return Messenger.getMessenger().error();
//		}
	}
}
