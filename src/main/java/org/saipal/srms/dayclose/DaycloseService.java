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
//		System.out.println(lgid);
		String cond="";
		if(!lgid.isBlank()) {
			cond+=" and t.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond+=" and t.bankorgid='"+acno+"'";
		}
		
		String cond1="";
		if(!lgid.isBlank()) {
			cond1+=" and t.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond1+=" and t.bankorgid='"+acno+"'";
		}
		
		String sql = "select *,(amountcr-amountdr) as balance from (select accountno,bankid,depositbranchid,accountname,accountnumber,palika,lgid,sum(amountcr) as amountcr,sum(amountdr) as amountdr from ("
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+ auth.getBranchId() +"  where  dc.id is null and t.dateint=format(getdate(),'yyyyMMdd')  and  t.bankid=? and t.ttype=1 and t.branchid=? and t.approved=1 "+cond 
				+" union"
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+ auth.getBranchId() +"   where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.ttype=1 and t.branchid=? and t.approved=1 "+cond 
				+ " union"
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amount as amountcr,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.depositdateint and dc.branchid="+ auth.getBranchId() +"   where  dc.id is null and  t.depositdateint=format(getdate(),'yyyyMMdd') and  t.bankid=?  and t.depositbranchid=? and t.approved=1 "+cond1
				+" ) a group by accountno,accountname,accountnumber,palika,lgid,bankid,depositbranchid) b ";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(auth.getBankId(),auth.getBranchId(),auth.getBankId(),auth.getBranchId(),auth.getBankId(),auth.getBranchId()));
//		System.out.println(admlvl.get(0).get("depositbranchid").toString());
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
					mapadmlvl.put("bankid", t.get("bankid"));
					mapadmlvl.put("branchid", t.get("depositbranchid"));
					
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
//		String cbid=request("corebankid");
		String lgid=request("lgid");
		String acno=request("acno");
		String items=request("selection");
		if (!items.startsWith("[")) {
			items = "[" + items + "]";
		}
		String corebank=request("corebank");
//		System.out.println(corebank);
		JSONObject cb = new JSONObject(corebank);
//		System.out.println(cb);
		JSONArray jarr = new JSONArray(items);
		
		if (jarr.length() > 0) {
			for (int i = 0; i < jarr.length(); i++) {
				String[] parts = jarr.get(i).toString().split("\\|\\|");
				String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and branchid=? and dateint=format(getdate(),'yyyyMMdd')";
				Map<String,Object> t = db.getSingleResultMap(sq,Arrays.asList(parts[0],parts[1],auth.getBranchId()));
				if(t.get("cid").toString().equals("0")) {
					String id=db.newIdInt();
//					System.out.println(cb.get(parts[1]));
					String sql = "insert into dayclose(id,lgid,bankorgid,accountno,accountname,amountdr,amountcr,bankid,branchid,creatorid,corebankid,dateint) values (?,?,?,?,?,?,?,?,?,?,?,format(getdate(),'yyyyMMdd')) ";
					DbResponse rowEffect = db.execute(sql,
							Arrays.asList(id,parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],auth.getBankId(),auth.getBranchId(),auth.getUserId(),cb.get(parts[1])));
//					System.out.println(rowEffect.getMessage());
					String sql1="select * from taxvouchers where lgid=? and bankorgid=? and branchid=? and ttype=1 and approved=1 and dateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> admlvl = db.getResultList(sql1, Arrays.asList(parts[0],parts[1],auth.getBranchId()));
					
					
					
					
					if (!admlvl.isEmpty()) {
						for (Tuple tt : admlvl) {
							String sql2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sql2,
									Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("bankorgid"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("amountdr"),tt.get("amountcr")));
						}
				}
					
					
					String sq1="select * from taxvouchers_log where lgid=? and bankorgid=? and branchid=? and ttype=1 and approved=1 and dateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> t1 = db.getResultList(sq1, Arrays.asList(parts[0],parts[1],auth.getBranchId()));
					
					if (!t1.isEmpty()) {
						for (Tuple tt : t1) {
							String sq2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sq2,
									Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("bankorgid"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("amountdr"),tt.get("amountcr")));
						}
				}
//					System.out.println("i am here");
					
					String sql3="select * from bank_deposits where lgid=? and bankorgid=? and paymentmethod=? and depositbranchid=? and approved=1 and depositdateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> t3 = db.getResultList(sql3, Arrays.asList(parts[0],parts[1],2,auth.getBranchId()));
					
					if (!t3.isEmpty()) {
						for (Tuple tt : t3) {
							String sql4 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sql4,
									Arrays.asList(id,tt.get("id"),tt.get("transactionid"),tt.get("bankvoucherno"),tt.get("voucherdate"),tt.get("taxpayername"),0,tt.get("taxpayername"),tt.get("mobileno"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("depositbranchid"),tt.get("bankorgid"),"",tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),1,0,"",0,0,0,tt.get("usestatus"),0,0,tt.get("amount")));
						}
				}
					
				}

			}
			return Messenger.getMessenger().success();
		}else {
			return Messenger.getMessenger().error();
		}

	}
	
	@Transactional
	public ResponseEntity<Map<String, Object>> daycloseScheduler() throws JSONException {
		// TODO Auto-generated method stub
		String date=request("date");
		String sql = "select *,(amountcr-amountdr) as balance from (select accountno,bankid,depositbranchid,accountname,accountnumber,palika,lgid,sum(amountcr) as amountcr,sum(amountdr) as amountdr from ("
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and (dc.dateint=t.dateint or dc.dateint=t.cleardateint)    where  dc.id is null and (t.dateint=format(getdate(),'yyyyMMdd') or t.cleardateint=format(getdate(),'yyyyMMdd'))   and ( t.approved=1 or t.cstatus=1) " 
				+" union"
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint    where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd')   and (t.approved=1 or t.cstatus=1) " 
				+ " union"
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amount as amountcr,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.depositdateint  where  dc.id is null and  t.depositdateint=format(getdate(),'yyyyMMdd') and  t.approved=1 "
				+" ) a group by accountno,accountname,accountnumber,palika,lgid,bankid,depositbranchid) b ";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		System.out.println(sql);
		
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and branchid=? and dateint=format(getdate(),'yyyyMMdd')";
				Map<String,Object> ttt = db.getSingleResultMap(sq,Arrays.asList(t.get("lgid"),t.get("accountnumber"),t.get("depositbranchid")));
				if(ttt.get("cid").toString().equals("0")) {
					String id=db.newIdInt();
					String sql1 = "insert into dayclose(id,lgid,bankorgid,accountno,accountname,amountdr,amountcr,bankid,branchid,creatorid,corebankid,dateint) values (?,?,?,?,?,?,?,?,?,?,?,format(getdate(),'yyyyMMdd')) ";
					DbResponse rowEffect = db.execute(sql1,
							Arrays.asList(id,t.get("lgid"),t.get("accountno"),t.get("accountnumber"),t.get("accountname"),t.get("amountdr"),t.get("amountcr"),t.get("bankid"),t.get("depositbranchid"),0,"system"));
					
					String sql2="select * from taxvouchers where lgid=? and bankorgid=? and branchid=?  and (approved=1 or cstatus=1) and (dateint=format(getdate(),'yyyyMMdd') or cleardateint=format(getdate(),'yyyyMMdd'))";
					List<Tuple> txv = db.getResultList(sql2, Arrays.asList(t.get("lgid"),t.get("accountno"),t.get("depositbranchid")));
					
					if (!txv.isEmpty()) {
					for (Tuple tt : txv) {
						String sql3 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
						db.execute(sql3,
								Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("bankorgid"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("amountdr"),tt.get("amountcr")));
					}
			}
					
					String sq1="select * from taxvouchers_log where lgid=? and bankorgid=? and branchid=?  and (approved=1 or cstatus=1) and dateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> t1 = db.getResultList(sq1, Arrays.asList(t.get("lgid"),t.get("accountno"),t.get("depositbranchid")));
					
					if (!t1.isEmpty()) {
						for (Tuple tt : t1) {
							String sq2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sq2,
									Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("bankorgid"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("amountdr"),tt.get("amountcr")));
						}
				}
					
					String sql3="select * from bank_deposits where lgid=? and bankorgid=? and paymentmethod=? and depositbranchid=? and approved=1 and depositdateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> t3 = db.getResultList(sql3, Arrays.asList(t.get("lgid"),t.get("accountno"),2,t.get("depositbranchid")));
					
					if (!t3.isEmpty()) {
						for (Tuple tt : t3) {
							String sql4 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sql4,
									Arrays.asList(id,tt.get("id"),tt.get("transactionid"),tt.get("bankvoucherno"),tt.get("voucherdate"),tt.get("taxpayername"),0,tt.get("taxpayername"),tt.get("mobileno"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("depositbranchid"),tt.get("bankorgid"),"",tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),1,0,"",0,0,0,tt.get("usestatus"),0,0,tt.get("amount")));
						}
				}
					
					
				}
			}
			return Messenger.getMessenger().success();
		}
		
		else {
			return Messenger.getMessenger().setMessage("No transaction Found").error();
		}
		

	}

	public ResponseEntity<Map<String, Object>> daycloseCustom() {
		// TODO Auto-generated method stub
		String dateint=request("date").replace("-","");
		String lgid=request("lgid");
		String acno=request("acno");
		
		String bankid=request("bankid");
		String branchid=request("branchid");
//		System.out.println(lgid);
		String cond="";
		if(!lgid.isBlank()) {
			cond+=" and t.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond+=" and t.bankorgid='"+acno+"'";
		}
		
		String cond1="";
		if(!lgid.isBlank()) {
			cond1+=" and t.lgid='"+lgid+"'";
		}
		if(!acno.isBlank()) {
			cond1+=" and t.bankorgid='"+acno+"'";
		}
		String sql = "select *,(amountcr-amountdr) as balance from (select accountno,bankid,depositbranchid,accountname,accountnumber,palika,lgid,sum(amountcr) as amountcr,sum(amountdr) as amountdr from ("
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and (dc.dateint=t.dateint or dc.dateint=t.cleardateint) and dc.branchid="+ branchid +"  where  dc.id is null   and  t.bankid=?  and t.branchid=?  and (t.dateint= " +dateint+ " or t.cleardateint= " +dateint+ ")   and ( t.approved=1 or t.cstatus=1) "+cond 
				+" union"
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+ branchid +"   where  dc.id is null and  t.dateint= " +dateint+ " and  t.bankid=? and t.branchid=? and ( t.approved=1 or t.cstatus=1) "+cond 
				+ " union"
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amount as amountcr,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.depositdateint and dc.branchid="+ branchid +"   where  dc.id is null and  t.depositdateint= " +dateint+ " and  t.bankid=?  and t.depositbranchid=? and t.approved=1 "+cond1
				+" ) a group by accountno,accountname,accountnumber,palika,lgid,bankid,depositbranchid) b ";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(bankid,branchid,bankid,branchid,bankid,branchid));
//		System.out.println(sql);
		
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				String sq="select count(id) as cid from dayclose where lgid=? and accountno=? and branchid=? and dateint=format(getdate(),'yyyyMMdd')";
				Map<String,Object> ttt = db.getSingleResultMap(sq,Arrays.asList(t.get("lgid"),t.get("accountnumber"),t.get("depositbranchid")));
				if(ttt.get("cid").toString().equals("0")) {
					String id=db.newIdInt();
					String sql1 = "insert into dayclose(id,lgid,bankorgid,accountno,accountname,amountdr,amountcr,bankid,branchid,creatorid,corebankid,dateint) values (?,?,?,?,?,?,?,?,?,?,?,format(getdate(),'yyyyMMdd')) ";
					DbResponse rowEffect = db.execute(sql1,
							Arrays.asList(id,t.get("lgid"),t.get("accountno"),t.get("accountnumber"),t.get("accountname"),t.get("amountdr"),t.get("amountcr"),t.get("bankid"),t.get("depositbranchid"),0,"system"));
					
					String sql2="select * from taxvouchers where lgid=? and bankorgid=? and branchid=?  and (approved=1 or cstatus=1) and (dateint=format(getdate(),'yyyyMMdd') or cleardateint=format(getdate(),'yyyyMMdd'))";
					List<Tuple> txv = db.getResultList(sql2, Arrays.asList(t.get("lgid"),t.get("accountno"),t.get("depositbranchid")));
					
					if (!txv.isEmpty()) {
					for (Tuple tt : txv) {
						String sql3 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
						db.execute(sql3,
								Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("bankorgid"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("amountdr"),tt.get("amountcr")));
					}
			}
					
					String sq1="select * from taxvouchers_log where lgid=? and bankorgid=? and branchid=?  and (approved=1 or cstatus=1) and dateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> t1 = db.getResultList(sq1, Arrays.asList(t.get("lgid"),t.get("accountno"),t.get("depositbranchid")));
					
					if (!t1.isEmpty()) {
						for (Tuple tt : t1) {
							String sq2 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sq2,
									Arrays.asList(id,tt.get("id"),tt.get("karobarsanket"),tt.get("voucherno"),tt.get("date"),tt.get("taxpayername"),tt.get("taxpayerpan"),tt.get("depositedby"),tt.get("depcontact"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("branchid"),tt.get("bankorgid"),tt.get("purpose"),tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),tt.get("ttype"),tt.get("chequebank"),tt.get("chequeno"),tt.get("chequeamount"),tt.get("cstatus"),tt.get("chequetype"),tt.get("isused"),tt.get("hasChangeReqest"),tt.get("amountdr"),tt.get("amountcr")));
						}
				}
					
					String sql3="select * from bank_deposits where lgid=? and bankorgid=? and paymentmethod=? and depositbranchid=? and approved=1 and depositdateint=format(getdate(),'yyyyMMdd')";
					List<Tuple> t3 = db.getResultList(sql3, Arrays.asList(t.get("lgid"),t.get("accountno"),2,t.get("depositbranchid")));
					
					if (!t3.isEmpty()) {
						for (Tuple tt : t3) {
							String sql4 = "insert into dayclose_details(dcid,tvid,karobarsanket,dateint,voucherno,date,taxpayername,taxpayerpan,depositedby,depcontact,lgid,collectioncenterid,bankid,branchid,accountno,purpose,syncstatus,approved,approverid,ttype,chequebank,chequeno,chequeamount,cstatus,chequetype,isused,hasChangeReqest,changeReqestDate,amountdr,amountcr) values (?,?,?,format(getdate(),'yyyyMMdd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL,?,?) ";
							db.execute(sql4,
									Arrays.asList(id,tt.get("id"),tt.get("transactionid"),tt.get("bankvoucherno"),tt.get("voucherdate"),tt.get("taxpayername"),0,tt.get("taxpayername"),tt.get("mobileno"),tt.get("lgid"),tt.get("collectioncenterid"),tt.get("bankid"),tt.get("depositbranchid"),tt.get("bankorgid"),"",tt.get("syncstatus"),tt.get("approved"),tt.get("approverid"),1,0,"",0,0,0,tt.get("usestatus"),0,0,tt.get("amount")));
						}
				}
					
					
				}
			}
			return Messenger.getMessenger().success();
		}
		
		else {
			return Messenger.getMessenger().setMessage("No transaction Found").error();
		}
	}

}
