package org.saipal.srms.report;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.saipal.srms.excel.Excel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("report")
public class ReportController {
	@Autowired
	ReportService rs;

	@GetMapping("get-fys")
	public ResponseEntity<Map<String, Object>> getFys() {
		return rs.getFys();
	}

	@GetMapping("get-branches")
	public ResponseEntity<Map<String, Object>> getBranches() {
		return rs.getBranches();
	}

	@GetMapping("get-llgs")
	public ResponseEntity<Map<String, Object>> getLocalLevels() {
		return rs.getLocalLevels();
	}
	
	@GetMapping("get-account-numbers")
	public ResponseEntity<Map<String, Object>> getAccountNumber() {
		return rs.getAccountNumbers();
	}
	
	@GetMapping("get-user")
	public ResponseEntity<Map<String, Object>> getUser() {
		return rs.getUsers();
	}

	@GetMapping("get-report")
	public void getReport(HttpServletResponse resp) throws IOException {
		String reporttype = rs.request("reporttype");
		Excel report = rs.getReport();
		if (report != null) {
			if (reporttype.equals("1")) { // htmlreport
				resp.setContentType("text/html; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				try {
					resp.getWriter().print(report.getHtmlDocument());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else { // excel report
				Workbook wb = report.getExcel();
				String fileName = "report.xlsx";
				resp.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				try {
					wb.write(resp.getOutputStream());
					wb.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			resp.getWriter().print("No Data Available.");
		}
	}
}
