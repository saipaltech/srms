package org.saipal.srms.vouchers;

import java.util.Map;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ApiController {
	
	
	@Autowired
	TaxPayerVoucherService tp;
	
	@Autowired
	BankVoucherService bv;
	
	@Autowired
	Authenticated auth;
	
	/*
	 * To Be Called by SuTRA application, to get the voucher details 
	 * if they are already not pushed to the Sutra
	 * */
	@GetMapping("get-voucherbyno")
	public ResponseEntity<String> getVoucher() {
		if(auth.canSystemApi()) {
			return  tp.getVoucherDetailsByVoucherNo();
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Invalid Request\"}");
	}
	
	@GetMapping("get-voucherbyid")
	public ResponseEntity<String> getVoucherByid() {
		if(auth.canSystemApi()) {
			return  tp.getVoucherDetailsByVoucherId();
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Invalid Request\"}");
	}
	
	/*
	 * To Be Called by SuTRA application, to get the deposit voucher details 
	 * using the payment reference number
	 * */
	@GetMapping("get-deposit-status")
	public ResponseEntity<Map<String, Object>> getVoucherStatus() {
		if(auth.canSystemApi()) {
			return  bv.getVoucherStatus();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
	}
	
	/*
	 * To Be Called by SuTRA application, to post bank voucher details 
	 * 
	 * */
	@PostMapping("bankvoucher")
	public ResponseEntity<Map<String, Object>> saveBankVoucher() {
		if(auth.canSystemApi()) {
			return  bv.saveBankVoucher();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
	}
}
