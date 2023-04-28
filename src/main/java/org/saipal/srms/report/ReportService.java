package org.saipal.srms.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.persistence.Tuple;

import org.saipal.srms.excel.Excel;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.Messenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportService extends AutoService {

	public ResponseEntity<Map<String, Object>> getFys() {
		String sql = "select distinct fyid from (select distinct fyid from taxvouchers union select distinct fyid from bank_deposits) a";
		List<Tuple> lt = db.getResultList(sql);
		List<Map<String, Object>> fys = new ArrayList<>();
		for (Tuple t : lt) {
			String fyid = t.get("fyid") + "";
			int fyint = Integer.parseInt(fyid);
			String fyText = (2060 + fyint) + "/" + ((2060 + fyint + 1) + "").substring(2);
			fys.add(Map.of("id", fyid, "label", fyText));
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
//		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "' and tx.lgid="+palika+" and tx.fyid="+fy+ " and tx.branchid="+branch;
		String condition = " WHERE dateint >= '" + startDate + "' AND dateint <= '" + endDate + "'";
		if (!fy.isBlank()) {
			condition  = condition + " and tx.fyid="+fy+" ";
		}
		if (!palika.isBlank())
			condition = condition + " and tx.lgid="+palika+" ";
		if (!branch.isBlank())
			condition = condition + " and tx.branchid="+branch+" ";
		
		condition = condition+" and tx.bankid="+ auth.getBankId();
		
		System.out.println(auth.getBankId());
		
		if (type.equals("cad")) {
			repTitle = getHeaderString("Cash Deposit, From:" + request("from") + " To:" + request("to"));
			sql = "SELECT tx.*,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid "
					+ condition + " and tx.approved=1 order by palika, ba.accountnumber";
		} else if (type.equals("chd")) {
			repTitle = getHeaderString("Cheque Deposit, From:" + request("from") + " To:" + request("to"));
			sql = "SELECT tx.*,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid"
					+ condition + " and tx.cstatus=1 order by palika, accountno";
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
		condition = condition+" and depositbankid="+ auth.getBankId();
		condition += " order by officename";
		String repTitle = "Verified Vouchers, From:" + request("from") + " To:" + request("to");
		String sql = "select * from bank_deposits " + condition;

		List<Tuple> lists = db.getResultList(sql);
		excl.title = repTitle;
		String OldPalika = "";
		float ptotal = 0;
		int totalAmount = 0;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N."))).addColumn((new Excel().ExcelCell("Office Name")))
				.addColumn((new Excel().ExcelCell("Account Number")))
				.addColumn((new Excel().ExcelCell("Karobar Sanket")))
				.addColumn((new Excel().ExcelCell("Voucher No.")))
				.addColumn((new Excel().ExcelCell("Voucher Date")))
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
					ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
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
						.addColumn((new Excel().ExcelCell(t.get("bankvoucherno") + "")))
						.addColumn((new Excel().ExcelCell(t.get("voucherdate") + "")))
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
		condition = condition+" and dc.bankid="+ auth.getBankId();
		condition = condition+ " order by palika ";
		String repTitle = "Day Close, From:" + request("from") + " To:" + request("to");
		String sql = "select dc.*, lls.namenp as palika, dcd.karobarsanket, (dc.amountcr-dc.amountdr) as balance from dayclose dc join admin_local_level_structure lls on lls.id = dc.lgid join dayclose_details dcd on dc.id = dcd.dcid "
				+ condition;
		List<Tuple> lists = db.getResultList(sql);
		excl.title = repTitle;
		String OldPalika = "";
		float ptotal = 0;
		float totalAmount = 0;
		Excel.excelRow hrow = new Excel().ExcelRow();
		hrow.addColumn((new Excel().ExcelCell("S.N."))).addColumn((new Excel().ExcelCell("Palika")))
		.addColumn((new Excel().ExcelCell("Account Number")))
		.addColumn((new Excel().ExcelCell("Karobar Sanket")))
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
			ptrow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
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
				.addColumn((new Excel().ExcelCell(t.get("karobarsanket") + "")))
				.addColumn((new Excel().ExcelCell(t.get("amountdr") + "")))
				.addColumn((new Excel().ExcelCell(t.get("amountcr") + "")))
				.addColumn((new Excel().ExcelCell(t.get("balance") + "")))
				.addColumn((new Excel().ExcelCell("<a href=\"/taxpayer-voucher/dayclose-details?id="+ t.get("id")+"\" target=\"_blank\">Details</a>")));
		excl.addRow(drow);
		i++;
	}
	if (totalAmount > 0) {
		Excel.excelRow trow = (new Excel().ExcelRow()).addColumn((new Excel().ExcelCell("Total", 6, 1)))
				.addColumn((new Excel().ExcelCell(totalAmount + "",2,1)));
		excl.addRow(trow);
	}
}
	return excl;
	}
}
