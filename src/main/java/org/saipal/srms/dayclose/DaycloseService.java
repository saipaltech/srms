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
			cond+=" and taxvouchers.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond+=" and taxvouchers.accountno='"+acno+"'";
		}
		
		String sql = "select accountno,accountname,accountnumber,palika,lgid,sum(amountcr) as amountcr,sum(amountdr) as amountdr from ("
				+ " select cast(accountno as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,amountcr, amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.accountno where  dateint=format(getdate(),'yyyyMMdd') and  t.bankid=?"
				+ " union"
				+ " select cast(accountno as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,amountcr, amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.accountno where  dateint=format(getdate(),'yyyyMMdd') and  t.bankid=?"
				+ " ) a group by accountno,accountname,accountnumber,palika,lgid";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(auth.getBankId(),auth.getBankId()));
		if(admlvl.isEmpty()) {
			return Messenger.getMessenger().setMessage("No transaction found").error();
		}else {
			List<Map<String, Object>> list = new ArrayList<>();
			if (!admlvl.isEmpty()) {
				for (Tuple t : admlvl) {
//					System.out.println(t.toString());
					Map<String, Object> mapadmlvl = new HashMap<>();
					mapadmlvl.put("accountno", t.get("accountno"));
					mapadmlvl.put("lgid", t.get("lgid"));
					mapadmlvl.put("amountcr", t.get("amountcr"));
					mapadmlvl.put("amountdr", t.get("amountdr"));
					mapadmlvl.put("accountname", t.get("accountname"));
					mapadmlvl.put("accountnumber", t.get("accountnumber"));
					mapadmlvl.put("palika", t.get("palika"));
					
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
		JSONArray jarr = new JSONArray(items);
		String id=db.newIdInt();
		if (jarr.length() > 0) {
			for (int i = 0; i < jarr.length(); i++) {
				String[] parts = jarr.get(i).toString().split("\\|\\|");
				String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
				Map<String,Object> t = db.getSingleResultMap(sq,Arrays.asList(parts[0],parts[1]));
				if(t.get("cid").toString().equals("0")) {
					String sql = "insert into dayclose(id,lgid,accountno,creatorid,corebankid,dateint) values (?,?,?,?,?,format(getdate(),'yyyyMMdd')) ";
					DbResponse rowEffect = db.execute(sql,
							Arrays.asList(id,parts[0],parts[1],auth.getUserId(),cbid));
					String sql1="select id, karobarsanket,taxpayername,amount from taxvouchers where lgid=? and accountno=? and dateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> admlvl = db.getResultList(sql1, Arrays.asList(parts[0],parts[1]));
					if (!admlvl.isEmpty()) {
						for (Tuple tt : admlvl) {
							String sql2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint) values (?,?,?,format(getdate(),'yyyyMMdd')) ";
							db.execute(sql2,
									Arrays.asList(id,tt.get("id"),tt.get("karobarsanket")));
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
