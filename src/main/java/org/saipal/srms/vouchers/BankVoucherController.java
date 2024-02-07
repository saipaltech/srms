package org.saipal.srms.vouchers;


import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.saipal.srms.util.Messenger;
import org.saipal.srms.util.ValidationService;
import org.saipal.srms.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank-voucher")
public class BankVoucherController {
	
	@Autowired
	BankVoucherService objService;

	@Autowired
	ValidationService validationService;
	
	@GetMapping("")
	
	  public ResponseEntity<Map<String, Object>> index(HttpServletRequest request)
	 { return objService.index();
	 }
	 
//	@PostMapping("")
//	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
//		Validator validator = validationService.validate(BankVoucher.rules());
//		if (validator.isFailed()) {
//			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
//		} else {
//			return objService.store();
//		}
//	}
	
//	@GetMapping()
//	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request)
//	 {
//		return objService.index(); 
//	 }

//	@GetMapping("")
//	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request) {
//		return objService.edit();
//	}

	@PostMapping("")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request) throws JSONException {
		Validator validator = validationService.validate(BankVoucher.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
		} else {
			return objService.update();
		}
	}
	
	@PostMapping("chequeDeposit")
	public ResponseEntity<Map<String, Object>> chequeDeposit(HttpServletRequest request) throws JSONException {
			return objService.chequeDeposit();
	}
	
	@PostMapping("reconcilation")
	public ResponseEntity<Map<String, Object>> reconcilation(HttpServletRequest request) throws JSONException {
			return objService.reconcilation();
	}
	
	
	@PostMapping("vouchercancel")
	public ResponseEntity<Map<String, Object>> vouchercancel(HttpServletRequest request) throws JSONException {
		Validator validator = validationService.validate(VoucherCancel.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
		} else {
			return objService.vouchercancel();
		}	
	}

//	@DeleteMapping("/{id}")
//	public ResponseEntity<Map<String, Object>> destroy(HttpServletRequest request, @PathVariable String id) {
//		return objService.destroy(id);
//	}
//	
	@GetMapping("search-payment")
	public ResponseEntity<Map<String, Object>> getList() {
		return objService.getTransDetails();
	}
	
	@GetMapping("submitToPalika")
	public ResponseEntity<Map<String, Object>> submitToPalika() {
		return objService.submitToPalika();
	}
	
	@GetMapping("deleteVoucher")
	public ResponseEntity<Map<String, Object>> deleteVoucher() {
		return objService.deleteVoucher();
	}
	
	@GetMapping("getreconcilation")
	public ResponseEntity<List<Map<String, Object>>>  getreconcilation() {
		return objService.getreconcilation();
	}
	
	@GetMapping("search-payment-sutra")
	public ResponseEntity<Map<String, Object>> getTransDetails() {
		return objService.getTransDetailsSutra();
	}
	

//	
//	@GetMapping("banks-from-sutra")
//	public ResponseEntity<String> getBanksFromSutra() {
//		return objService.getBanksFromSutra();
//	}
}
