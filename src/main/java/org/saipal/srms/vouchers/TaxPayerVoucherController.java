package org.saipal.srms.vouchers;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.codehaus.jettison.json.JSONException;
import org.saipal.srms.excel.Excel;
import org.saipal.srms.report.ReportService;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.ValidationService;
import org.saipal.srms.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/taxpayer-voucher")
public class TaxPayerVoucherController {
	
	@Autowired
	TaxPayerVoucherService objService;

	@Autowired
	ValidationService validationService;
	
	@Autowired
	ReportService rs;
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
//		System.out.println("Reached at Index");
		return objService.index();
	}
	@GetMapping("cheque")
	public ResponseEntity<Map<String, Object>> index1(HttpServletRequest request) {
//		System.out.println("Reached at Index");
		return objService.indexcheque();
	}
	
	@GetMapping("getDirectbank")
	public ResponseEntity<Map<String, Object>> getDirectbank(HttpServletRequest request) {
		return objService.getDirectbank();
	}
	
	@GetMapping("vouchercancel")
	public ResponseEntity<Map<String, Object>> vouchercancel(HttpServletRequest request) {
//		System.out.println("Reached at Index");
		return objService.vouchercancel();
	}
	
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) throws JSONException {
		Validator validator = validationService.validate(TaxPayerVoucher.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
		} else {
			return objService.store();
		}
	}
	
	

	@GetMapping("{id}")
	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request, @PathVariable String id) {
		return objService.edit(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) throws JSONException {
		Validator validator = validationService.validate(TaxPayerVoucher.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
		} else {
			return objService.update(id);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> destroy(HttpServletRequest request, @PathVariable String id) {
		return objService.destroy(id);
	}
	
	@PostMapping("approve/{id}")
	public ResponseEntity<Map<String, Object>> approveVoucher(@PathVariable String id) {
		return objService.approveVoucher(id);
	}
	
	@PostMapping("approvedirectbankvoucher/{id}")
	public ResponseEntity<Map<String, Object>> approvedirectbankvoucher(@PathVariable String id) throws JSONException {
		return objService.approvedirectbankvoucher(id);
	}
	
	@GetMapping("get-list")
	public ResponseEntity<List<Map<String, Object>>> getList() {
		return objService.getList();
	}
	
	@GetMapping("get-local-levels")
	public ResponseEntity<String> getLocalLevelsFromSutra() {
		return objService.getLocalLevels();
	}
	
	@GetMapping("get-local-levels-all")
	public ResponseEntity<String> getLocalLevelsAll() {
		return objService.getLocalLevelsAll();
	}
	
	@GetMapping("get-local-levels-all-cheque")
	public ResponseEntity<String> getLocalLevelsAllCheque() {
		return objService.getLocalLevelsAllCheque();
	}
	
	@GetMapping("get-all-local-levels")
	public ResponseEntity<String> getAllLocalLevels() {
		return objService.getAllLocalLevels();
	}
	
	@GetMapping("get-cost-centres")
	public ResponseEntity<String> getCostCentres() {
		return objService.getCostCentres();
	}
	
	@GetMapping("get-bank-accounts")
	public ResponseEntity<String> getBankAccounts() {
		return objService.getBankAccounts();
	}
	@GetMapping("get-bank-accounts-off")
	public ResponseEntity<String> getBankAccountsOff() {
		return objService.getBankAccountsOff();
	}
	
	
	
	@GetMapping("search-voucher")
	public ResponseEntity<Map<String, Object>> searchVoucher() {
//		System.out.println("here");
		return objService.searchVoucher();
	}
	
	
	
	
	@GetMapping("chequeclear")
	public ResponseEntity<Map<String, Object>> chequeclear() {
		return objService.chequeclear();
	}
	
	@GetMapping("get-revenue-list")
	public ResponseEntity<String> getRevenue() {
		return objService.getRevenue();
	}
	
	@GetMapping("get-banks-list")
	public ResponseEntity<String> getBank() {
		return objService.getBank();
	}
	
	@GetMapping("llg-details")
	public ResponseEntity<String> getAllDetails() {
		return objService.getAllDetails();
	}
	
	@GetMapping("pan-details")
	public ResponseEntity<Map<String, Object>> getPanDetails() {
		return objService.getPanDetails();
	}
	@GetMapping("get-specific/{id}")
	public ResponseEntity<Map<String, Object>> getSpecific(HttpServletRequest request, @PathVariable String id){
		return objService.getSpecific(id);
	}
	
	@GetMapping("get-specific-own/{id}")
	public ResponseEntity<Map<String, Object>> getSpecificOwn(HttpServletRequest request, @PathVariable String id){
		return objService.getSpecificOwn(id);
	}
	
	@GetMapping("generate-report")
	public ResponseEntity<Map<String, Object>> generateReport(HttpServletRequest request){
		return objService.generateReport();
	}
	
	@GetMapping("getRevenueDetails")
	public ResponseEntity<Map<String, Object>> getRevenueDetails(HttpServletRequest request){
		return objService.getRevenueDetails();
	}
	
	@GetMapping("get-usertype")
	public ResponseEntity<Map<String, Object>> getusertype(HttpServletRequest request){
		return objService.getusertype();
	}
	
	@GetMapping("get-edit-detail")
	public ResponseEntity<Map<String, Object>> getEditDetail(){
		return objService.getEditDetails();
	}
	@PostMapping("update-details")
	public ResponseEntity<Map<String, Object>> saveEditDetails() throws JSONException{
		return objService.saveEditDetails();
	}
	
	@GetMapping("get-edit-detail-off")
	public ResponseEntity<Map<String, Object>> getEditDetailOff(){
		return objService.getEditDetailsOff();
	}
	@PostMapping("update-details-off")
	public ResponseEntity<Map<String, Object>> saveEditDetailsOff(){
		return objService.saveEditDetailsOff();
	}

	@GetMapping("vocuher-transfer")
	public ResponseEntity<Map<String, Object>> voucherIndex(HttpServletRequest request) {
		return objService.getVoucherTransfer();
	}
	
	@PostMapping("get-report")
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
		//Map<String,Object> data = objService.getReport().getBody();
		
		//return new ModelAndView(data.get("view")+"",Map.of("rdata",data));
		
	}
	
	@PostMapping("get-report-default-branch")
	public void getReportDefaultBranch(HttpServletResponse resp) throws IOException {
		String reporttype = rs.request("reporttype");
		Excel report = rs.getReportDefaultBranch();
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
		//Map<String,Object> data = objService.getReport().getBody();
		
		//return new ModelAndView(data.get("view")+"",Map.of("rdata",data));
		
	}
	
	@GetMapping("dayclose-details")
	public void getReportDayClose(HttpServletResponse resp) throws IOException{
		String reporttype = "1";//rs.request("reporttype");
		Excel report = rs.getDetails();
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
	
	@GetMapping("dayclosecheque-details")
	public void getReportDayCloseCheque(HttpServletResponse resp) throws IOException{
		String reporttype = "1";//rs.request("reporttype");
		Excel report = rs.getDetailsCheque();
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
	@GetMapping("get-reportf")
	public ModelAndView getReportf(HttpServletRequest request) {
		return  new ModelAndView("report-cad");
	}
	@GetMapping("get-specific-another-palika/{id}")
	public ResponseEntity<Map<String, Object>> getSpecificAnotherPalika(HttpServletRequest request, @PathVariable String id){
		return objService.getSpecificAnotherPalika(id);
	}
	
	@PostMapping("settle-updates")
	public ResponseEntity<Map<String, Object>> settlePalikaChange(){
		return objService.settlePalikaChange();
	}
	
	@GetMapping("report-generate")
	public ModelAndView reportGenerate() {
		Map<String,Object> dt = objService.generateReport().getBody();
		Map<String,Object> revd = objService.getRevenueDetails().getBody();
//		Map<String,Object> rev1= (Map)((List)revd.get("data")).get(0);
		Map<String,Object> data = Map.of("data",dt,"revd",revd.get("data"));
		if((dt.get("ttype")+"").equals("2")) {
			return new ModelAndView("cheque-bank-copy",data);
		}else {
			return new ModelAndView("voucher-bank-copy",data);
		}
		
	}
	
	@GetMapping("get-branch-report")
	public ResponseEntity<Map<String, Object>> getBranchReport(HttpServletRequest request){
		return objService.getBranchForReport();
	}
}
