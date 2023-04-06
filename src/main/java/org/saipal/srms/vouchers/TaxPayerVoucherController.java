package org.saipal.srms.vouchers;


import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;

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

@RestController
@RequestMapping("/taxpayer-voucher")
public class TaxPayerVoucherController {
	
	@Autowired
	TaxPayerVoucherService objService;

	@Autowired
	ValidationService validationService;
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		System.out.println("Reached at Index");
		return objService.index();
	}

	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
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
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) {
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
	
	@GetMapping("get-list")
	public ResponseEntity<List<Map<String, Object>>> getList() {
		return objService.getList();
	}
	
	@GetMapping("get-local-levels")
	public ResponseEntity<String> getLocalLevelsFromSutra() {
		return objService.getLocalLevels();
	}
	
	@GetMapping("get-cost-centres")
	public ResponseEntity<String> getCostCentres() {
		return objService.getCostCentres();
	}
	
	@GetMapping("get-bank-accounts")
	public ResponseEntity<String> getBankAccounts() {
		return objService.getBankAccounts();
	}
	
	@GetMapping("get-revenue-list")
	public ResponseEntity<String> getRevenue() {
		return objService.getRevenue();
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
	public ResponseEntity<List<Map<String, Object>>> getSpecific(HttpServletRequest request, @PathVariable String id){
		return objService.getSpecific(id);
	}
	
	@GetMapping("generate-report")
	public ResponseEntity<Map<String, Object>> generateReport(HttpServletRequest request){
		return objService.generateReport();
	}
}
