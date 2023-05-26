package org.saipal.srms.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
		String sql = " select fyid,(case when fyid=dbo.getfyid('') then 1 else 0 end) as isdef from (select distinct fyid from (select distinct fyid from taxvouchers union select distinct fyid from bank_deposits) a)b";
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
					+" union"
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,karobarsanket,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+branchid+"  where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.ttype=1 and t.branchid=? "+cond 
					+ " union"
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
					+" union"
					+" select  cast(t.bankorgid as varchar) as accountno,b.accountname,b.accountnumber,ll.namenp as palika,karobarsanket,cast(t.lgid as varchar) as lgid,t.amountcr, t.amountdr,t.taxpayername,t.chequeno from taxvouchers_log t join admin_local_level_structure ll on ll.id=t.lgid join bankaccount b on b.id=t.bankorgid  left join dayclose dc on dc.lgid=t.lgid and dc.bankorgid=t.bankorgid and dc.dateint=t.dateint and dc.branchid="+branchid+"  where  dc.id is null and  t.dateint=format(getdate(),'yyyyMMdd') and  t.bankid=? and t.ttype=2 and t.cstatus=1 and t.branchid=? "+cond 
//					+ " union"
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
		String repTitle = "<span>SuTRA Revenue Module: Bank Interface</sapan> <br/>";
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
		
		System.out.println(auth.getBankId());
		
		if (type.equals("cad")) {
			repTitle = getHeaderString("Cash Deposit, From:" + request("from") + " To:" + request("to"));
			sql = "SELECT tx.*,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid "
					+ condition + " and tx.approved=1 order by palika, ba.accountnumber";
		} else if (type.equals("chd")) {
			if (!chkstatus.isEmpty()) {
				condition = condition + " and tx.cstatus="+chkstatus+" ";
			}
			repTitle = getHeaderString("Cheque Deposit, From:" + request("from") + " To:" + request("to"));
			sql = "SELECT tx.*,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid"
					+ condition + " order by palika, accountno";
		}
		excl.title = repTitle;
		List<Tuple> lists = db.getResultList(sql);
		String OldPalika = "";
		float ptotal = 0;
		float totalAmount = 0;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N."))).addColumn((new Excel().ExcelCell("Palika")))
				.addColumn((new Excel().ExcelCell("Account Number")))
				.addColumn((new Excel().ExcelCell("Account Name")))
				.addColumn((new Excel().ExcelCell("Karobar Sanket")))
				.addColumn((new Excel().ExcelCell("TaxPayer")))
				.addColumn((new Excel().ExcelCell("Amount")));
		excl.addHeadRow(hrow);
		if (!lists.isEmpty()) {
			int i = 1;
			for (Tuple t : lists) {
				totalAmount += (Float.parseFloat(t.get("amount") + ""));
				if (OldPalika.isBlank()) {
					OldPalika = t.get("palika") + "";
				}
				Excel.excelRow ptrow = null;
				if (!OldPalika.equals(t.get("palika") + "")) {
					ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
							.addColumn((new Excel().ExcelCell(ptotal + "")));
					OldPalika = t.get("palika") + "";
					ptotal = Float.parseFloat(t.get("amount") + "");
				} else {
					ptotal += Float.parseFloat(t.get("amount") + "");
				}
				if (ptrow != null) {
					excl.addRow(ptrow);
				}
				Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
						.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountname") + "")))
						.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
						.addColumn((new Excel().ExcelCell(t.get("taxpayername") + "")))
						.addColumn((new Excel().ExcelCell(t.get("amount") + "")));
				excl.addRow(drow);
				i++;
			}
			if (totalAmount > 0) {
				Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
						.addColumn((new Excel().ExcelCell(totalAmount + "")));
				excl.addRow(trow);
			}
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
		String sql = "select * from bank_deposits " + condition;
		List<Tuple> lists = db.getResultList(sql);
		excl.title = repTitle;
		String OldPalika = "";
		float ptotal = 0;
		int totalAmount = 0;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N.")))
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
				totalAmount += (Float.parseFloat(t.get("amount") + ""));
				if (OldPalika.isBlank()) {
					OldPalika = t.get("officename") + "";
				}
				Excel.excelRow ptrow = null;
				if (!OldPalika.equals(t.get("officename") + "")) {
					ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 4, 1)))
							.addColumn((new Excel().ExcelCell(ptotal + "")));
					OldPalika = t.get("officename") + "";
					ptotal = Float.parseFloat(t.get("amount") + "");
				} else {
					ptotal += Float.parseFloat(t.get("amount") + "");
				}
				if (ptrow != null) {
					excl.addRow(ptrow);
				}
				Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
						.addColumn((new Excel().ExcelCell(t.get("officename") + "")))
						.addColumn((new Excel().ExcelCell(t.get("accountnumber") + "")))
						.addColumn((new Excel().ExcelCell(t.get("transactionid") + "")))
						//.addColumn((new Excel().ExcelCell(t.get("bankvoucherno") + "")))
						//.addColumn((new Excel().ExcelCell(t.get("voucherdate") + "")))
						.addColumn((new Excel().ExcelCell(t.get("amount") + "")));
				excl.addRow(drow);
				i++;
			}
			if (totalAmount > 0) {
				Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 4, 1)))
						.addColumn((new Excel().ExcelCell(totalAmount + "")));
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
			condition = condition + " and deposituserid="+username+" ";
		condition = condition+" and dc.bankid="+ auth.getBankId();
		String repTitle = getHeaderString("Day Close, From:" + request("from") + " To:" + request("to"));
		String sql = "select dc.id,dc.lgid,dc.accountno,dc.dateint,dc.amountcr,dc.amountdr,dc.bankorgid,lls.namenp as palika,(dc.amountcr-dc.amountdr) as balance from dayclose dc join admin_local_level_structure lls on lls.id = dc.lgid join dayclose_details dcd on dc.id = dcd.dcid "
				+condition
				+" group by dc.id,dc.lgid,dc.accountno,dc.dateint,dc.amountcr,dc.amountdr,dc.bankorgid,lls.namenp"
				+ " order by palika ";
		List<Tuple> lists = db.getResultList(sql);
		excl.title = repTitle;
		String OldPalika = "";
		float ptotal = 0;
		float totalAmount = 0;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N."))).addColumn((new Excel().ExcelCell("Palika")))
		.addColumn((new Excel().ExcelCell("Account Number")))
		.addColumn((new Excel().ExcelCell("Debit")))
		.addColumn((new Excel().ExcelCell("Credit"))).addColumn((new Excel().ExcelCell("Balance")))
		.addColumn((new Excel().ExcelCell("Details")));
excl.addHeadRow(hrow);
if (!lists.isEmpty()) {
	int i = 1;
	for (Tuple t : lists) {
		totalAmount += (Float.parseFloat(t.get("balance") + ""));
		if (OldPalika.isBlank()) {
			OldPalika = t.get("palika") + "";
		}
		Excel.excelRow ptrow = null;
		if (!OldPalika.equals(t.get("palika") + "")) {
			ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 5, 1)))
					.addColumn((new Excel().ExcelCell(ptotal + "", 2,1)));
			OldPalika = t.get("palika") + "";
			ptotal = Float.parseFloat(t.get("balance") + "");
		} else {
			ptotal += Float.parseFloat(t.get("balance") + "");
		}
		if (ptrow != null) {
			excl.addRow(ptrow);
		}
		Excel.excelRow drow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell((i + ""))))
				.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
				.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
				.addColumn((new Excel().ExcelCell(t.get("amountdr") + "")))
				.addColumn((new Excel().ExcelCell(t.get("amountcr") + "")))
				.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
				.addColumn((new Excel().ExcelCell("<a href=\"/taxpayer-voucher/dayclose-details?id="+ t.get("id")+"\" target=\"_blank\">Details</a>")));
		excl.addRow(drow);
		i++;
	}
	if (totalAmount > 0) {
		Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 5, 1)))
				.addColumn((new Excel().ExcelCell(totalAmount + "",2,1)));
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
	public Excel getSr(Excel ecxl) {
		return null;
	}
}
