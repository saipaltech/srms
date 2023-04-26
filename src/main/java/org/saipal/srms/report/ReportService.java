package org.saipal.srms.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.srms.excel.Excel;
import org.saipal.srms.service.AutoService;
import org.saipal.srms.util.Messenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportService extends AutoService{

	public ResponseEntity<Map<String, Object>> getFys() {
		return Messenger.getMessenger().setData(Map.of("id",18,"label","2078/79","id",19,"label","2079/80","id",20,"label","2080/81")).success();
	}
	
	public ResponseEntity<Map<String, Object>> getLocalLevels() {
		List<Map<String,Object>> d = db.getResultListMap(
				"select distinct als.id,als.nameen,als.namenp from admin_local_level_structure als join bankaccount ba on als.id=ba.lgid and bankid=? order by als.namenp",
				Arrays.asList(auth.getBankId(), auth.getBranchId()));
		return Messenger.getMessenger().setData(d).success();
	}
	
	public ResponseEntity<Map<String, Object>> getBranches() {
		String cond="";
		if (!auth.hasPermission("bankhq")) {
			cond +="and id='"+auth.getBranchId()+"'";
		}
		List<Map<String,Object>> d = db.getResultListMap(
				"select distinct * from branches where bankid=? "+cond+" order by name",
				Arrays.asList(auth.getBankId()));
		return Messenger.getMessenger().setData(d).success();
	}

	public Excel getReport() {
		Excel excl = new Excel();
		excl.subtitle="";
		String startDate = request("from").replace("-", "");
		String endDate = request("to").replace("-", "");
		String type = request("type")+"";
		String repTitle="";
		String sql = "";
		String condition= " WHERE dateint >= '"+startDate+"' AND dateint <= '"+endDate+"'";
		if (type.equals("cad")) {
			repTitle = "Cash Deposit, From:"+request("from")+" To:"+request("to");
			sql = "SELECT tx.*,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid " + condition +" and tx.approved=1";
		}
		else if (type.equals("chd")) {
			repTitle = "Cheque Deposit, From:"+request("from")+" To:"+request("to");
			sql = "SELECT tx.*,lls.namenp as palika ,tx.amountcr as amount,ba.accountnumber as accountno, ba.accountname FROM taxvouchers tx join bankaccount ba on ba.id=tx.bankorgid join admin_local_level_structure lls on lls.id=tx.lgid" + condition + " and tx.cstatus=1";
			}
		else if (type.equals("vv")) {
			repTitle = "Verified Vouchers, From:"+request("from")+" To:"+request("to");
			sql = "select * from bank_deposits WHERE depositdateint >= '"+startDate+"' AND depositdateint <= '"+endDate+"'";
		}
		else if (type.equals("dc")) {
			repTitle = "Day Close, From:"+request("from")+" To:"+request("to");
			sql = "select dc.*, lls.namenp as palika, (amountcr-amountdr) as balance from dayclose dc join admin_local_level_structure lls on lls.id = dc.lgid " + condition;
		}
		else {
			sql = "SELECT * FROM taxvouchers " + condition;
		}
		excl.title =repTitle;

		List<Tuple> lists = db.getResultList(sql);
				Excel.excelRow hrow = new Excel().ExcelRow();
				
				hrow.addColumn((new Excel().ExcelCell("S.N.")))
						.addColumn((new Excel().ExcelCell("Palika")))
						.addColumn((new Excel().ExcelCell("Account Number")))
						.addColumn((new Excel().ExcelCell("Account Name")))
						.addColumn((new Excel().ExcelCell("Voucher No.")))
						.addColumn((new Excel().ExcelCell("TaxPayer")))
						.addColumn((new Excel().ExcelCell("Amount")));
				excl.addHeadRow(hrow);
				if (!lists.isEmpty()) {
					int i = 1;
					for (Tuple t : lists) {
						Excel.excelRow drow = (new Excel().ExcelRow())
								.addColumn((new Excel().ExcelCell((i + ""))))
								.addColumn((new Excel().ExcelCell(t.get("palika") + "")))
								.addColumn((new Excel().ExcelCell(t.get("accountno") + "")))
								.addColumn((new Excel().ExcelCell(t.get("accountname")+ "")))
								.addColumn((new Excel().ExcelCell(t.get("voucherno") + "")))
								.addColumn((new Excel().ExcelCell(t.get("taxpayername") + "")))
								.addColumn((new Excel().ExcelCell(t.get("amount") + "")));
						excl.addRow(drow);
						i++;
					}
				}
		return excl;
	}
}
