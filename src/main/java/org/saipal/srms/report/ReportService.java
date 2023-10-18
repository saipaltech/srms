package org.saipal.srms.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.persistence.Tuple;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.excel.Excel;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportService extends AutoService {
	@Autowired
	Authenticated auth;
	public ResponseEntity<Map<String, Object>> getFys() {
		String sql = " select fyid,(case when fyid=dbo.getfyid('') then 1 else 0 end) as isdef from (select distinct fyid from (select distinct fyid from taxvouchers union all select distinct fyid from bank_deposits) a)b";
		List<Tuple> lt = db.getResultList(sql);
		List<Map<String, Object>> fys = new ArrayList<>();
		for (Tuple t : lt) {
			String fyid = t.get("fyid") + "";
			int fyint = Integer.parseInt(fyid);
			String fyText = (2060 + fyint) + "/" + ((2060 + fyint + 1) + "").substring(2);
			fys.add(Map.of("id", fyid, "label", fyText,"isdef",t.get("isdef")+""));
		}
		return Messenger.getMessenger().setData(fys).success();
	}

	public ResponseEntity<Map<String, Object>> getLocalLevels() {
		List<Map<String, Object>> d = db.getResultListMap(
				"select distinct cast(als.id as varchar) as id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? order by als.namenp",
				Arrays.asList(auth.getBankId()));
		return Messenger.getMessenger().setData(d).success();
	}

	public ResponseEntity<Map<String, Object>> getBranches() {
		String cond = "";
		if (!auth.hasPermission("bankhq")) {
			cond += "and id='" + auth.getBranchId() + "'";
		}
		List<Map<String, Object>> d = db.getResultListMap(
				"select distinct * from branches where bankid=? " + cond + " order by name",
				Arrays.asList(auth.getBankId()));
		return Messenger.getMessenger().setData(d).success();
	}

	public Excel getReport() {
		Excel excl = new Excel();
		String type = request("type") + "";
		if (type.equals("cad") || type.equals("chd")) {
			return getCadChd(excl);
		}
		else if (type.equals("vv")) {
			getVV(excl);
		} else if (type.equals("dc")) {
			getDc(excl);
		} else if (type.equals("sr")) {
			getSr(excl);
		}
		
		return excl;

	}
	
	public Excel getReportDefaultBranch(){
		Excel excl = new Excel();
		String type = request("type") + "";
		if (type.equals("dbracdr")) {
			return getDefaultBranchDetailReport(excl);
		}
		else if (type.equals("dbracr")) {
			return getDefaultBranchReport(excl);
		} else if (type.equals("obcr")) {
			return getOffBranchCollectionReport(excl);
		} else if (type.equals("obcrs")) {
			return getOffBranchCollectionReportSummary(excl);
		}
		else if (type.equals("obcfob")) {
			return getobcfob(excl);
		}
		else if (type.equals("obcfobs")) {
			return getobcfobs(excl);
		}
		else if (type.equals("llrcr")) {
			return getllrcr(excl);
		}
		else if (type.equals("dcr")) {
			return getdcr(excl);
		}
		return excl;		
	}
	
	public Excel getDetails() {
		Excel excl = new Excel();
		String id = request("id");
		String sql = null;
//		System.out.println(auth.getBranchId());
		List<Tuple> data;
		String repTitle="";
		
		if (!id.isBlank()) {
			sql = "select dc.*, lls.namenp as palika, (amountcr-amountdr) as balance from dayclose_details dc join admin_local_level_structure lls on lls.id = dc.lgid where dcid='"+id+"'";
			data = db.getResultList(sql);
		}
		
		else {
			String lgid = request("lgid");
			String bankorgid = request("bankorgid");
			String bankid=request("bankid");
			String branchid=request("branchid");
			String cond="";
			
			if(!lgid.isBlank()) {
				cond+=" and t.lgid='"+lgid+"'";
			}
			if(!bankorgid.isBlank()) {
				cond+=" and t.bankorgid='"+bankorgid+"'";
			}
			
			String cond1="";
			if(!lgid.isBlank()) {
				cond1+=" and t.lgid='"+lgid+"'";
			}
			if(!bankorgid.isBlank()) {
				cond1+=" and t.bankorgid='"+bankorgid+"'";
			}
			sql = "select * from (select accountno,accountname,accountnumber,palika,lgid,amountdr,amountcr,karobarsanket from ("
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,karobarsanket,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+branchid+"   where  dc.id is null and t.dateint=format(getdate(),'yyyyMMdd')  and  t.bankid=? and t.ttype=1 and t.branchid=? "+cond 
					+" union all "
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,karobarsanket,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+branchid+"  where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.ttype=1 and t.branchid=? "+cond 
					+ " union all "
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,transactionid as karobarsanket,cast(t.lgid as varchar) as lgid,t.amount as amountcr,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.depositdateint and dc.branchid="+branchid+"   where  dc.id is null and  t.depositdateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.depositbranchid=? "+cond1
					+" ) a ) b ";
			data = db.getResultList(sql,Arrays.asList(bankid,branchid,bankid,branchid,bankid,branchid));
		}
		repTitle = "Day Close Details";
		excl.title = repTitle;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow
		.addColumn((new Excel().ExcelCell("S.N.")))
				.addColumn((new Excel().ExcelCell("Karobar Sanket")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")));
		excl.addHeadRow(hrow);
		int i=1;
		for (Tuple t : data) {
			
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
					.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
					.addColumn((new Excel().ExcelCell(t.get("amountdr") + "")))
					.addColumn((new Excel().ExcelCell(t.get("amountcr") + "")));
			excl.addRow(drow);
			i++;
		}
		return excl;

	}
	
	public Excel getDetailsCheque() {
		Excel excl = new Excel();
		String id = request("id");
		String sql = null;
		List<Tuple> data;
		String repTitle="";
//		System.out.println(auth.getBranchId());
		
		if (!id.isBlank()) {
			sql = "select dc.*, lls.namenp as palika, (amountcr-amountdr) as balance,dc.taxpayername,dc.chequeno from dayclose_details dc join admin_local_level_structure lls on lls.id = dc.lgid where dcid='"+id+"'";
			data = db.getResultList(sql);
		}
		
		else {
			String lgid = request("lgid");
			String bankorgid = request("bankorgid");
			String bankid=request("bankid");
			String branchid=request("branchid");
			String cond="";
			
			if(!lgid.isBlank()) {
				cond+=" and t.lgid='"+lgid+"'";
			}
			if(!bankorgid.isBlank()) {
				cond+=" and t.bankorgid='"+bankorgid+"'";
			}
			
			String cond1="";
			if(!lgid.isBlank()) {
				cond1+=" and t.lgid='"+lgid+"'";
			}
			if(!bankorgid.isBlank()) {
				cond1+=" and t.bankorgid='"+bankorgid+"'";
			}
			sql = "select * from (select accountno,accountname,accountnumber,palika,lgid,amountdr,amountcr,karobarsanket,taxpayername,chequeno from ("
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,karobarsanket,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr,t.taxpayername,t.chequeno from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+branchid+"  where  dc.id is null and t.dateint=format(getdate(),'yyyyMMdd')  and  t.bankid=? and t.ttype=2 and t.cstatus=1 and t.branchid=? "+cond 
					+" union all "
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,karobarsanket,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr,t.taxpayername,t.chequeno from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+branchid+"  where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.ttype=2 and t.cstatus=1 and t.branchid=? "+cond 
//					+ " union all "
//					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,transactionid as karobarsanket,cast(t.lgid as varchar) as lgid,t.amount as amountcr,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.depositdateint   where  dc.id is null and  t.depositdateint=format(getdate(),'yyyyMMdd') and  t.bankid=?  and t.depositbranchid=?"+cond1
					+" ) a ) b ";
			data = db.getResultList(sql,Arrays.asList(bankid,branchid,bankid,branchid));
		}
		repTitle = "Day Close Details";
		excl.title = repTitle;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow
		.addColumn((new Excel().ExcelCell("S.N.")))
				.addColumn((new Excel().ExcelCell("Karobar Sanket")))
				.addColumn((new Excel().ExcelCell("Taxpayer Name")))
				.addColumn((new Excel().ExcelCell("Cheque Number")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")));
		excl.addHeadRow(hrow);
		int i=1;
		for (Tuple t : data) {
			
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
					.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
					.addColumn((new Excel().ExcelCell(t.get("taxpayername") + "")))
					.addColumn((new Excel().ExcelCell(t.get("chequeno") + "")))
					.addColumn((new Excel().ExcelCell(t.get("amountdr") + "")))
					.addColumn((new Excel().ExcelCell(t.get("amountcr") + "")));
			excl.addRow(drow);
			i++;
		}
		return excl;

	}
	private String getHeaderString(String title) {
		String repTitle = "<span>SuTRA Revenue Module: Bank Interface</span> <br/>";
		repTitle += "<span>"+db.getSingleResult("select namenp from bankinfo where id="+auth.getBankId()).get(0)+"</span><br/>";
		repTitle += "<span>"+db.getSingleResult("select name from branches where id="+auth.getBranchId()).get(0)+"</span><br/>";
		return repTitle+title;
	}

	private Excel getCadChd(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String type = request("type") + "";
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String branch= request("branch")+"";
		String repTitle="";
		String sql = "";
		String chkstatus= request("chkstatus")+"";
		//		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "' and tx.lgid="+palika+" and tx.fyid="+fy+ " and tx.branchid="+branch;
		if (type.equals("cad")) {
			String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		}else {
			String condition = " WHERE cleardateint >= '" + startDate + "' AND cleardateint <= '" + endDate + "'";
		}
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank())
			condition = condition + " and tx.lgid="+palika+" ";
		if (!branch.isBlank())
			condition = condition + " and tx.branchid="+branch+" ";
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
		
//		System.out.println(auth.getBankId());
		
		if (type.equals("cad")) {
			repTitle = getHeaderString("Cash Deposit, From:" + request("from") + " To:" + request("to"));
			sql = "SELECT tx.*,cast(tx.date as Date) as dates,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid "
					+ condition + " and tx.approved=1 order by palika, ba.accountnumber";
		} else if (type.equals("chd")) {
			if (!chkstatus.isEmpty()) {
				condition = condition + " and tx.cstatus="+chkstatus+" ";
			}
			repTitle = getHeaderString("Cheque Deposit, From:" + request("from") + " To:" + request("to"));
			sql = "SELECT tx.*,cast(tx.date as Date) as dates,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid"
					+ condition + " and tx.ttype=2 and tx.cstatus=1 order by palika, accountno";
		}
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);
		String OldPalika = "";
		BigDecimal ptotal = new BigDecimal("0");
		BigDecimal totalAmount = new BigDecimal("0");
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N.")))
				.addColumn((new Excel().ExcelCell("Date")))
				.addColumn((new Excel().ExcelCell("Palika")))
				.addColumn((new Excel().ExcelCell("Account Number")))
				.addColumn((new Excel().ExcelCell("Account Name")))
				.addColumn((new Excel().ExcelCell("Karobar Sanket")))
				.addColumn((new Excel().ExcelCell("TaxPayer")))
				.addColumn((new Excel().ExcelCell("Amount")));
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			int i = 1;
			for (Tuple t : lists) {
				totalAmount = totalAmount.add(new BigDecimal(t.get("amount") + ""));
				if (OldPalika.isBlank()) {
					OldPalika = t.get("palika") + "";
				}
				Excel.excelRow ptrow = null;
				if (!OldPalika.equals(t.get("palika") + "")) {
					ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 7, 1)))
							.addColumn((new Excel().ExcelCell(ptotal + "")));
					OldPalika = t.get("palika") + "";
					ptotal = new BigDecimal(t.get("amount") + "");
				} else {
					ptotal = ptotal.add( new BigDecimal(t.get("amount") + ""));
				}
				if (ptrow != null) {
					excl.addRow(ptrow);
				}
				Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
						.addColumn((new Excel().ExcelCell(t.get("dates") + "")))
						.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountname") + "")))
						.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
						.addColumn((new Excel().ExcelCell(t.get("taxpayername") + "")))
						.addColumn((new Excel().ExcelCell(t.get("amount") + "")));
				excl.addRow(drow);
				i++;
			}
			if (totalAmount.compareTo(BigDecimal.valueOf(0d))==1) {
				Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 7, 1)))
						.addColumn((new Excel().ExcelCell(totalAmount.toPlainString())));
				excl.addRow(trow);
			}
		}

		return excl;
	}
	
	private Excel getDefaultBranchDetailReport(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String type = request("type") + "";
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String branch= request("branch")+"";
		String accno= request("accno")+"";
		String repTitle="";
		String sql = "";
		String chkstatus= request("chkstatus")+"";
		//		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "' and tx.lgid="+palika+" and tx.fyid="+fy+ " and tx.branchid="+branch;
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and tx.lgid="+palika+" ";
		}else {
			condition = condition + " and tx.lgid in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		if (!accno.isBlank())
			condition = condition + " and tx.bankorgid="+accno+" ";
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
		condition = condition+" and tx.depositbranchid="+ auth.getBranchId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		}else {
			condition1 = condition1 + " and tx.lgid in (select dlgid from branches  where id= "+ auth.getBranchId()+")";
		}
		if (!accno.isBlank())
			condition1 = condition1 + " and tx.bankorgid="+accno+" ";
//		String username= request("users")+"";
		if (!username.isBlank())
			condition1 = condition1 + " and deposituserid="+username+" ";
		
		condition1 = condition1+" and tx.bankid="+ auth.getBankId();
		condition1 = condition1+" and tx.depositbranchid="+ auth.getBranchId();

		sql = "select * from (select accountno,accountname,palika,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
				+" ) a ) b order by tdate,medium";

	
			repTitle = getHeaderString("Default Branch revenue account collection detail report, From:" + request("from") + " To:" + request("to"));
//			sql = "SELECT tx.*,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) order by palika, ba.accountnumber";
		
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);
		if (!lists.isEmpty()) {
			
			Excel.excelRow hrow0 = new Excel().ExcelRow();
			hrow0	.addColumn((new Excel().ExcelCell("AC No.: "+lists.get(0).get("accountno"),3)))
					.addColumn((new Excel().ExcelCell("AC Name: "+lists.get(0).get("accountname"),2)))
					.addColumn((new Excel().ExcelCell("Palika: "+lists.get(0).get("palika"),2)));
			excl.addHeadRow(hrow0);
			excl.addHeadRow((new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("",7))));
			Excel.excelRow hrow = new Excel().ExcelRow();
			hrow	.addColumn((new Excel().ExcelCell("Date")))
					.addColumn((new Excel().ExcelCell("VoucherID")))
					.addColumn((new Excel().ExcelCell("Collection Media")))
					.addColumn((new Excel().ExcelCell("Debit")))
					.addColumn((new Excel().ExcelCell("Credit")))
					.addColumn((new Excel().ExcelCell("Balance")))
					.addColumn((new Excel().ExcelCell("Collecting branch")));
					
			excl.addHeadRow(hrow);
			
			int i = 1;
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
						.addColumn((new Excel().ExcelCell(t.get("branch") + "")));
				excl.addRow(drow);
				i++;
			}
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",3)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString(),2)));
			excl.addRow(drow);
		}
		return excl;
	}


	private Excel getVV(Excel excl) {
		excl.subtitle = "";
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String branch= request("branch")+"";
		
		String condition = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '"
				+ endDate +"'";
		if (!fy.isBlank()) {
			condition  = condition + " and fyid="+fy+" ";		}
		if (!palika.isBlank())
			condition = condition + " and lgid="+palika+" ";
		if (!branch.isBlank())
			condition = condition + " and depositbranchid="+branch+" ";
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		condition = condition+" and depositbankid="+ auth.getBankId();
		condition += " order by officename,accountnumber";
		String repTitle = getHeaderString("Verified Vouchers, From:" + request("from") + " To:" + request("to"));
		String sql = "select *,cast(voucherdate as Date) as dates from bank_deposits " + condition;
		List<Tuple> lists = db.getResultList(sql);
		excl.title = repTitle;
		String OldPalika = "";
		BigDecimal ptotal = new BigDecimal("0");
		BigDecimal totalAmount = new BigDecimal("0");
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N.")))
		.addColumn((new Excel().ExcelCell("Date")))
		.addColumn((new Excel().ExcelCell("Office Name")))
				.addColumn((new Excel().ExcelCell("Account Number")))
				.addColumn((new Excel().ExcelCell("Karobar Sanket")))
				//.addColumn((new Excel().ExcelCell("Voucher No.")))
				//.addColumn((new Excel().ExcelCell("Voucher Date")))
				.addColumn((new Excel().ExcelCell("Amount")));
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			int i = 1;
			for (Tuple t : lists) {
				totalAmount = totalAmount.add(new BigDecimal(t.get("amount") + ""));
				if (OldPalika.isBlank()) {
					OldPalika = t.get("officename") + "";
				}
				Excel.excelRow ptrow = null;
				if (!OldPalika.equals(t.get("officename") + "")) {
					ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 5, 1)))
							.addColumn((new Excel().ExcelCell(ptotal + "")));
					OldPalika = t.get("officename") + "";
					ptotal = new BigDecimal(t.get("amount") + "");
				} else {
					ptotal = ptotal.add(new BigDecimal(t.get("amount") + ""));
				}
				if (ptrow != null) {
					excl.addRow(ptrow);
				}
				Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
						.addColumn((new Excel().ExcelCell(t.get("dates") + "")))
						.addColumn((new Excel().ExcelCell(t.get("officename") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountnumber") + "")))
						.addColumn((new Excel().ExcelCell(t.get("transactionid") + "")))
						//.addColumn((new Excel().ExcelCell(t.get("bankvoucherno") + "")))
						//.addColumn((new Excel().ExcelCell(t.get("voucherdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("amount") + "")));
				excl.addRow(drow);
				if(i==lists.size()) {
					excl.addRow((new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 5, 1)))
							.addColumn((new Excel().ExcelCell(ptotal.toPlainString()))));
				}
				i++;
			}
			if (!totalAmount.toPlainString().equals("0")) {
				Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 5, 1)))
						.addColumn((new Excel().ExcelCell(totalAmount.toPlainString())));
				excl.addRow(trow);
			}
		}

		return excl;
	}
	
	private Excel getDc(Excel excl) {
		excl.subtitle = "";
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String branch= request("branch")+"";
		String condition = " WHERE dc.dateint >= '" + startDate + "' AND dc.dateint <= '" + endDate + "'";
		if (!fy.isBlank()) {
			condition  = condition + " and dc.fyid="+fy+" ";
		}
		if (!palika.isBlank())
			condition = condition + " and dc.lgid="+palika+" ";
		if (!branch.isBlank())
			condition = condition + " and dc.branchid="+branch+" ";
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and creatorid="+username+" ";
		condition = condition+" and dc.bankid="+ auth.getBankId();
		String repTitle = getHeaderString("Day Close, From:" + request("from") + " To:" + request("to"));
		String sql = "select dc.id,cast(dcd.date as Date) as dates,dc.lgid,dc.accountno,dc.dateint,dc.amountcr,dc.amountdr,dc.bankorgid,lls.namenp as palika,(dc.amountcr-dc.amountdr) as balance from dayclose dc join admin_local_level_structure lls on lls.id = dc.lgid join dayclose_details dcd on dc.id = dcd.dcid "
				+condition
				+" group by dc.id,dcd.date,dc.lgid,dc.accountno,dc.dateint,dc.amountcr,dc.amountdr,dc.bankorgid,lls.namenp"
				+ " order by palika ";
		List<Tuple> lists = db.getResultList(sql);
		System.out.println(sql);
		excl.title = repTitle;
		String OldPalika = "";
		BigDecimal ptotal = new BigDecimal("0");
		BigDecimal totalAmount = new BigDecimal("0");
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N.")))
		.addColumn((new Excel().ExcelCell("Date")))
		.addColumn((new Excel().ExcelCell("Palika")))
		.addColumn((new Excel().ExcelCell("Account Number")))
		.addColumn((new Excel().ExcelCell("Debit")))
		.addColumn((new Excel().ExcelCell("Credit"))).addColumn((new Excel().ExcelCell("Balance")))
		.addColumn((new Excel().ExcelCell("Details")));
excl.addHeadRow(hrow);
if (!lists.isEmpty()) {
	int i = 1;
	for (Tuple t : lists) {
		totalAmount =totalAmount.add(new BigDecimal(t.get("balance") + ""));
		if (OldPalika.isBlank()) {
			OldPalika = t.get("palika") + "";
		}
		Excel.excelRow ptrow = null;
		if (!OldPalika.equals(t.get("palika") + "")) {
			ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
					.addColumn((new Excel().ExcelCell(ptotal + "", 2,1)));
			OldPalika = t.get("palika") + "";
			ptotal = new BigDecimal(t.get("balance") + "");
		} else {
			ptotal = ptotal.add(new BigDecimal(t.get("balance") + ""));
		}
		if (ptrow != null) {
			excl.addRow(ptrow);
		}
		Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
				.addColumn((new Excel().ExcelCell(t.get("dates") + "")))
				.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
				.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
				.addColumn((new Excel().ExcelCell(t.get("amountdr") + "")))
				.addColumn((new Excel().ExcelCell(t.get("amountcr") + "")))
				.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
				.addColumn((new Excel().ExcelCell("<a href=\"/taxpayer-voucher/dayclose-details?id="+ t.get("id")+"\" target=\"_blank\">Details</a>")));
		excl.addRow(drow);
		i++;
	}
	if (totalAmount.compareTo(BigDecimal.valueOf(0d))==1) {
		Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
				.addColumn((new Excel().ExcelCell(totalAmount.toPlainString(),2,1)));
		excl.addRow(trow);
	}
}
	return excl;
	}

	public ResponseEntity<Map<String, Object>> getAccountNumbers() {
		String llgCode = request("llgcode");
		List<Map<String, Object>> d = db.getResultListMap(
				"select ba.accountname,cast(ba.accountnumber as varchar) as accountnumber,cast(ba.id as varchar) as id from bankaccount ba where ba.bankid=? and ba.lgid=? order by accounttype ",
				Arrays.asList(auth.getBankId(), llgCode));
		return Messenger.getMessenger().setData(d).success();
	}
	
	public ResponseEntity<Map<String, Object>> getUsers() {
		List<Map<String, Object>> d = db.getResultListMap(
				"select id,username from users where bankid=? and branchid=? ",
				Arrays.asList(auth.getBankId(), auth.getBranchId()));
		return Messenger.getMessenger().setData(d).success();
	}
	private Excel getSr(Excel excl) {
		excl.subtitle = "";
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String branch= request("branch")+"";
		String condition = " and t.dateint >= '" + startDate + "' AND t.dateint <= '" + endDate + "'";
		String condition1 = " and t.depositdateint >= '" + startDate + "' AND t.depositdateint <= '" + endDate + "'";
		if (!fy.isBlank()) {
			condition  = condition + " and t.fyid="+fy+" ";
			condition1  = condition1 + " and t.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and t.lgid="+palika+" ";
		 condition1 = condition1 + " and t.lgid="+palika+" ";
		}
		if (!branch.isBlank()) {
			condition = condition + " and t.branchid="+branch+" ";
		condition1 = condition1 + " and t.depositbranchid="+branch+" ";
		}
		String username= request("users")+"";
		if (!username.isBlank()) {
			condition = condition + " and deposituserid="+username+" ";
		condition1 = condition1 + " and deposituserid="+username+" ";
		}
		condition = condition+" and t.bankid="+ auth.getBankId();
		condition1 = condition1+" and t.bankid="+ auth.getBankId();
		String repTitle = getHeaderString("Summary Report, From:" + request("from") + " To:" + request("to"));
//		repTitle = getHeaderString("Cheque Deposit, From:" + request("from") + " To:" + request("to"));
		String sql = " select sum(cash) as cash,sum(cheque) as cheque from (select (case when ttype=1 then sum(amountcr-amountdr) else 0 end) as cash,(case when ttype=2 then sum(amountcr-amountdr) else 0 end) as cheque   from (select accountno,bankid,depositbranchid,accountname,accountnumber,ttype,palika,lgid,sum(amountcr) as amountcr,sum(amountdr) as amountdr from ("
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr,t.ttype, t.amountdr from taxvouchers t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid   where   t.bankid=?  and t.branchid=? and (t.approved=1 or t.cstatus=1) "+condition 
				+" union all "
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amountcr,t.ttype, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid     where  t.bankid=? and t.ttype=1 and t.branchid=? and t.approved=1 "+condition 
				+ " union all "
				+" select  cast(t.bankorgid as varchar) as accountno,t.depositbranchid,t.bankid,b.accountname,b.accountnumber,ll.namenp as palika,cast(t.lgid as varchar) as lgid,t.amount as amountcr,1 as ttype,0 as  amountdr from bank_deposits t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid    where   t.bankid=?  and t.depositbranchid=? and t.approved=1 "+condition1
				+" ) a group by accountno,accountname,accountnumber,palika,lgid,bankid,depositbranchid,ttype) c group by ttype) d";
		List<Tuple> lists = db.getResultList(sql, Arrays.asList(auth.getBankId(),auth.getBranchId(),auth.getBankId(),auth.getBranchId(),auth.getBankId(),auth.getBranchId()));
		excl.title = repTitle;
		String OldPalika = "";
		BigDecimal ptotal = new BigDecimal("0");
		BigDecimal totalAmount = new BigDecimal("0");
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("Cash"))).addColumn((new Excel().ExcelCell("Cheque")))
				.addColumn((new Excel().ExcelCell("Total")));
				
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			int i = 1;
			for (Tuple t : lists) {
				totalAmount = totalAmount.add((new BigDecimal(t.get("cash") + "")).add(new BigDecimal(t.get("cheque") + "")));
				
				
				
				Excel.excelRow drow = (new Excel().ExcelRow())
						
						.addColumn((new Excel().ExcelCell(t.get("cash") + "")))
						.addColumn((new Excel().ExcelCell(t.get("cheque") + "")))
						.addColumn((new Excel().ExcelCell(totalAmount.toPlainString())));
						
				excl.addRow(drow);
				i++;
			}
			
		}

		return excl;
	}

	
	private Excel getDefaultBranchReport(Excel excl) {

		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String accno= request("accno")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and tx.lgid="+palika+" ";
		}else {
			condition = condition + " and tx.lgid in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		String username= request("users")+"";
		if (!accno.isBlank())
			condition = condition + " and tx.bankorgid="+accno+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		}else {
			condition1 = condition1 + " and tx.lgid in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		if (!accno.isBlank())
			condition1 = condition1 + " and tx.bankorgid="+accno+" ";
		condition = condition+" and tx.depositbranchid="+ auth.getBranchId();
		condition1 = condition1+" and tx.depositbranchid="+ auth.getBranchId();
		condition1 = condition1+" and tx.bankid="+ auth.getBankId();

		sql = "select tdate,accountno,accountname,palika,sum(debit) as debit,sum(credit) as credit,medium,(sum(credit)-sum(debit)) as balance,branch from (select accountno,accountname,palika,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1 and tx.paymentmethod='2'"
				+" ) a ) b group by tdate,accountno,accountname,palika,branch,medium order by tdate,medium,branch";

	
			repTitle = getHeaderString("Default Branch revenue account collection report, From:" + request("from") + " To:" + request("to"));
		
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);
		
		if (!lists.isEmpty()) {
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
			Excel.excelRow hrow0 = new Excel().ExcelRow();
			hrow0	.addColumn((new Excel().ExcelCell("AC No.: "+lists.get(0).get("accountno"),2)))
					.addColumn((new Excel().ExcelCell("AC Name: "+lists.get(0).get("accountname"),2)))
					.addColumn((new Excel().ExcelCell("Palika: "+lists.get(0).get("palika"),2)));
			excl.addHeadRow(hrow0);
			excl.addHeadRow((new Excel().ExcelRow()).addColumn((new Excel().ExcelCell(" ",6))));
			Excel.excelRow hrow = new Excel().ExcelRow();
			hrow	.addColumn((new Excel().ExcelCell("Date")))
					.addColumn((new Excel().ExcelCell("Collection Media")))
					.addColumn((new Excel().ExcelCell("Debit")))
					.addColumn((new Excel().ExcelCell("Credit")))
					.addColumn((new Excel().ExcelCell("Balance")))
					.addColumn((new Excel().ExcelCell("Collecting branch")));
					
			excl.addHeadRow(hrow);
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow())
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
						.addColumn((new Excel().ExcelCell(t.get("branch") + "")));
				
						
				excl.addRow(drow);
				i++;
			}
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",2)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString(),2)));
			excl.addRow(drow);
		}
		return excl;
	}
	
	private Excel getOffBranchCollectionReport(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		String accno= request("accno")+"";
		if (!accno.isBlank())
					condition = condition + " and tx.bankorgid="+accno+" ";
		if (!accno.isBlank())
					condition1 = condition1 + " and tx.bankorgid="+accno+" ";
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and tx.lgid="+palika+" ";
		}else {
			condition = condition + " and tx.lgid not in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
		condition = condition+" and tx.depositbranchid = "+ auth.getBranchId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		}else {
			condition1 = condition1 + " and tx.lgid not in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		if (!username.isBlank())
			condition1 = condition1 + " and deposituserid="+username+" ";
		
		condition1 = condition1+" and tx.bankid="+ auth.getBankId();
		condition1 = condition1+" and tx.depositbranchid = "+ auth.getBranchId();
		

		sql = "select accountno,accountname,palika,ad.namenp as district,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from (select accountno,accountname,districtid,palika,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
				+" ) a ) b join admin_district ad on ad.districtid=b.districtid ";


			repTitle = getHeaderString("Off branch Collection Report, From:" + request("from") + " To:" + request("to"));
		
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);

		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow	.addColumn((new Excel().ExcelCell("Date")))
		.addColumn((new Excel().ExcelCell("District")))
				.addColumn((new Excel().ExcelCell("Palika")))
				.addColumn((new Excel().ExcelCell("Voucher Id")))
				.addColumn((new Excel().ExcelCell("AccountNumber")))
				.addColumn((new Excel().ExcelCell("AccountNumDesc")))
				.addColumn((new Excel().ExcelCell("Collection Media")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")))
				.addColumn((new Excel().ExcelCell("Balance")));
				
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow())
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("district") + "")))
						.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
						.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountname") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
						;
						
				excl.addRow(drow);
				i++;
			}
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",7)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString())));
			excl.addRow(drow);
		}
		return excl;
	}

	private Excel getOffBranchCollectionReportSummary(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		String accno= request("accno")+"";
		if (!accno.isBlank())
					condition = condition + " and tx.bankorgid="+accno+" ";
		if (!accno.isBlank())
					condition1 = condition1 + " and tx.bankorgid="+accno+" ";
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and tx.lgid="+palika+" ";
		}else {
			condition = condition + " and tx.lgid not in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
		condition = condition+" and tx.depositbranchid = "+ auth.getBranchId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		}else {
			condition1 = condition1 + " and tx.lgid not in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		if (!username.isBlank())
			condition1 = condition1 + " and deposituserid="+username+" ";
		
		condition1 = condition1+" and tx.bankid="+ auth.getBankId();
		condition1 = condition1+" and tx.depositbranchid = "+ auth.getBranchId();
		

		sql = "select b.accountno,b.accountname,b.palika,ad.namenp as district,sum(b.debit) as debit,sum(b.credit) as credit,medium,(sum(b.credit)-sum(b.debit)) as balance,b.tdate"
				+ " from (select accountno,accountname,palika,districtid,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
				+" ) a ) b join admin_district ad on ad.districtid=b.districtid"
				+ " group by b.accountno,b.accountname,b.palika,ad.namenp,b.medium,b.tdate order by b.tdate,ad.namenp,b.palika,b.medium";
			repTitle = getHeaderString("Off branch Collection Summary Report, From:" + request("from") + " To:" + request("to"));
		
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);

		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow	.addColumn((new Excel().ExcelCell("Date")))
		.addColumn((new Excel().ExcelCell("District")))
				.addColumn((new Excel().ExcelCell("Palika")))
				.addColumn((new Excel().ExcelCell("AccountNumber")))
				.addColumn((new Excel().ExcelCell("AccountNumDesc")))
				.addColumn((new Excel().ExcelCell("Collection Media")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")))
				.addColumn((new Excel().ExcelCell("Balance")));
				
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow())
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("district") + "")))
						.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountname") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")));
						
				excl.addRow(drow);
				i++;
			}
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",6)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString())));
			excl.addRow(drow);
		}
		return excl;
	}

	private Excel getobcfob(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and tx.lgid="+palika+" ";
		}else {
			condition = condition + " and tx.lgid  in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		
//		condition = condition+" and tx.bankid="+ auth.getBankId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		}else {
			condition1 = condition1 + " and tx.lgid  in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		if (!username.isBlank())
			condition1 = condition1 + " and deposituserid="+username+" ";
		
//		condition1 = condition1+" and tx.bankid="+ auth.getBankId();
		condition = condition+" and tx.depositbranchid <> "+ auth.getBranchId();
		condition1 = condition1+" and tx.depositbranchid <> "+ auth.getBranchId();

		sql = "select * from (select accountno,accountname,palika,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
				+" ) a ) b ";


			repTitle = getHeaderString("Outside branch Collection for own branch, From:" + request("from") + " To:" + request("to"));
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);
		if (!lists.isEmpty()) {
		Excel.excelRow hrow0 = new Excel().ExcelRow();
		hrow0	.addColumn((new Excel().ExcelCell("AC No.: "+lists.get(0).get("accountno"),3)))
				.addColumn((new Excel().ExcelCell("AC Name: "+lists.get(0).get("accountname"),2)))
				.addColumn((new Excel().ExcelCell("Palika: "+lists.get(0).get("palika"),2)));
		excl.addHeadRow(hrow0);
		excl.addHeadRow((new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("",7))));
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow	.addColumn((new Excel().ExcelCell("Date")))
		.addColumn((new Excel().ExcelCell("Voucher Id")))
				.addColumn((new Excel().ExcelCell("Collection Media")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")))
				.addColumn((new Excel().ExcelCell("Balance")))
				.addColumn((new Excel().ExcelCell("Collecting Branch")));
				
		excl.addHeadRow(hrow);
		}
		if (!lists.isEmpty()) {
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow())
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
						.addColumn((new Excel().ExcelCell(t.get("branch") + "")));
						
				excl.addRow(drow);
				i++;
			}
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",3)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString(),2)));
			excl.addRow(drow);
		}
		
		return excl;
	}

	private Excel getobcfobs(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition = condition + " and tx.lgid="+palika+" ";
		}else {
			condition = condition + " and tx.lgid  in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		String username= request("users")+"";
		if (!username.isBlank())
			condition = condition + " and deposituserid="+username+" ";
		
		condition = condition+" and tx.depositbranchid <>"+ auth.getBranchId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank()) {
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		}else {
			condition1 = condition1 + " and tx.lgid  in (select dlgid from branches where id= "+ auth.getBranchId()+")";
		}
		if (!username.isBlank())
			condition1 = condition1 + " and deposituserid="+username+" ";
		
		condition1 = condition1+" and tx.depositbranchid <>"+ auth.getBranchId();

		sql = "select * from (select accountno,accountname,palika,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
				+" ) a ) b ";


			repTitle = getHeaderString("Outside branch Collection for own branch Summary, From:" + request("from") + " To:" + request("to"));
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);
		if (!lists.isEmpty()) {
		Excel.excelRow hrow0 = new Excel().ExcelRow();
		hrow0	.addColumn((new Excel().ExcelCell("AC No.: "+lists.get(0).get("accountno"),3)))
				.addColumn((new Excel().ExcelCell("AC Name: "+lists.get(0).get("accountname"),2)))
				.addColumn((new Excel().ExcelCell("Palika: "+lists.get(0).get("palika"),2)));
		excl.addHeadRow(hrow0);
		excl.addHeadRow((new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("",7))));
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow	.addColumn((new Excel().ExcelCell("Date")))
				.addColumn((new Excel().ExcelCell("Collection Media")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")))
				.addColumn((new Excel().ExcelCell("Balance")))
				.addColumn((new Excel().ExcelCell("Collecting Branch")));
				
		excl.addHeadRow(hrow);
		}
		if (!lists.isEmpty()) {
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow())
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
						.addColumn((new Excel().ExcelCell(t.get("branch") + "")));
						
				excl.addRow(drow);
				i++;
			}
			
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",2)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString(),2)));
			excl.addRow(drow);
		}
		return excl;
	}

	private Excel getllrcr(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		String condition1 = " WHERE depositdateint >= '" + startDate + "' AND depositdateint <= '" + endDate + "'";
		
		String accno= request("accno")+"";
		if (!accno.isBlank())
					condition = condition + " and tx.bankorgid="+accno+" ";
		if (!accno.isBlank())
					condition1 = condition1 + " and tx.bankorgid="+accno+" ";
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank())
			condition = condition + " and tx.lgid="+palika+" ";
		String branches= request("branches")+"";
		if (!branches.isBlank())
			condition = condition + " and tx.depositbranchid="+branches+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
//		condition = condition+" and tx.depositbranchid <> "+ auth.getBranchId();
		
		
		if (!fy.isBlank()) {
			condition1  = condition1 + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank())
			condition1 = condition1 + " and tx.lgid="+palika+" ";
		if (!branches.isBlank())
			condition1 = condition1 + " and tx.depositbranchid="+branches+" ";
		
		condition1 = condition1+" and tx.bankid="+ auth.getBankId();
//		condition1 = condition1+" and tx.depositbranchid <> "+ auth.getBranchId();
		

//		sql = "select * from (select accountno,accountname,sum(debit) as debit,sum(credit) as credit,medium,sum(balance) as balance,tdate,branch from ("
//				+" SELECT (case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
//				+" union all "
//				+" SELECT (case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
//				+ " union all "
//				+" SELECT (case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
//				+" ) a ) b  ";
		sql = "select b.accountno,b.accountname,b.palika,sum(b.debit) as debit,sum(b.credit) as credit,medium,(sum(b.credit)-sum(b.debit)) as balance,b.tdate,b.branch"
				+ " from (select accountno,accountname,palika,districtid,debit,credit,karobarsanket,medium,balance,tdate,branch from ("
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+" union all "
				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
				+ " union all "
				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,lls.districtid,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
				+" ) a ) b "
				+ " group by b.accountno,b.accountname,b.palika,b.medium,b.tdate,b.branch order by b.tdate,b.palika,b.medium";


			repTitle = getHeaderString("Local Level Revenue Collection Report, From:" + request("from") + " To:" + request("to"));
		
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);

		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow	.addColumn((new Excel().ExcelCell("Date")))
				.addColumn((new Excel().ExcelCell("Collecting Branch")))
				.addColumn((new Excel().ExcelCell("AccountNumber")))
				.addColumn((new Excel().ExcelCell("AccountNumDesc")))
				.addColumn((new Excel().ExcelCell("Collection Media")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")))
				.addColumn((new Excel().ExcelCell("Balance")));
				
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			BigDecimal dtotal=new BigDecimal("0");
			BigDecimal ctotal=new BigDecimal("0");
			BigDecimal total=new BigDecimal("0");
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow())
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("branch") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountname") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
						;
						
				excl.addRow(drow);
				i++;
			}
			Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",5)))
					.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
					.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
					.addColumn((new Excel().ExcelCell(total.toPlainString())));
			excl.addRow(drow);
		}
		return excl;
	}


	private Excel getdcr(Excel excl) {
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String fy= request("fy")+"";
		String palika= request("palika")+"";
		String repTitle="";
		String sql = "";
		String condition = " WHERE tx.dateint >= '" + startDate + "' AND tx.dateint <= '" + endDate + "'";
		BigDecimal dtotal=new BigDecimal("0");
		BigDecimal ctotal=new BigDecimal("0");
		BigDecimal total=new BigDecimal("0");
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank())
			condition = condition + " and tx.lgid="+palika+" ";
		String username= request("users")+"";
		String accno=request("accno")+"";
		if (!accno.isBlank())
			condition = condition + " and dc.bankorgid="+accno+" ";
	
		condition = condition+" and tx.bankid="+ auth.getBankId();
		
		
		sql="SELECT lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate ,sum (tx.amountcr) as credit,sum(tx.amountdr) as debit,sum(tx.amountcr)-sum(tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM dayclose_details tx join dayclose dc on dc.id=tx.dcid  join bankaccount ba on ba.id= dc.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.branchid "+ condition + "  group by lls.namenp,tx.date,ba.accountnumber,ba.accountname,tx.ttype";

//		sql = "select * from (select accountno,accountname,palika,debit,credit,karobarsanket,medium,balance,tdate,branch,taxpayername from ("
//				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
//				+" union all "
//				+" SELECT tx.karobarsanket,tx.taxpayername,lls.namenp as palika,(case when tx.ttype='1' then 'Cash' else 'Cheque' end) as medium,cast(tx.date as date) as tdate,branches.name as branch ,tx.amountcr as credit,tx.amountdr as debit,(tx.amountcr-tx.amountdr) as balance,ba.accountnumber as accountno, ba.accountname FROM taxvouchers_log tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid"+ condition + " and (tx.approved=1 or tx.cstatus=1) " 
//				+ " union all "
//				+" SELECT tx.transactionid as taxpayername,tx.taxpayername,lls.namenp as palika,(case when tx.paymentmethod='2' then 'Cash' else 'Cheque' end) as medium,cast(tx.depositdate as date) as tdate,branches.name as branch ,tx.amount as credit,0 as debit,tx.amount as balance,ba.accountnumber as accountno, ba.accountname FROM bank_deposits tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid join branches on branches.id=tx.depositbranchid "+ condition1 + " and tx.approved=1  "
//				+" ) a ) b ";


			repTitle = getHeaderString("Day Close Report, From:" + request("from") + " To:" + request("to"));
		
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);

		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow	.addColumn((new Excel().ExcelCell("SN")))
		.addColumn((new Excel().ExcelCell("Date")))
				.addColumn((new Excel().ExcelCell("Palika")))
				.addColumn((new Excel().ExcelCell("AccountNumber")))
				.addColumn((new Excel().ExcelCell("AccNumDesc")))
				.addColumn((new Excel().ExcelCell("CollectionMedia")))
				.addColumn((new Excel().ExcelCell("Debit")))
				.addColumn((new Excel().ExcelCell("Credit")))
				.addColumn((new Excel().ExcelCell("Balance")));
				
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			
			int i = 1;
			for (Tuple t : lists) {
				dtotal =dtotal.add(new BigDecimal(t.get("debit")+""));
				ctotal =ctotal.add(new BigDecimal(t.get("credit")+""));
				total =total.add(new BigDecimal(t.get("balance")+""));
				Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
						.addColumn((new Excel().ExcelCell(t.get("tdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountname") + "")))
						.addColumn((new Excel().ExcelCell(t.get("medium") + "")))
						.addColumn((new Excel().ExcelCell(t.get("debit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("credit") + "")))
						.addColumn((new Excel().ExcelCell(t.get("balance") + "")));
				
				
				
						
				excl.addRow(drow);
				
				i++;
			}
		}
		Excel.excelRow crow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total",6)))
				.addColumn((new Excel().ExcelCell(dtotal.toPlainString() )))
				.addColumn((new Excel().ExcelCell(ctotal.toPlainString())))
				.addColumn((new Excel().ExcelCell(total.toPlainString(),2)));
		excl.addRow(crow);
		return excl;
	}

}

