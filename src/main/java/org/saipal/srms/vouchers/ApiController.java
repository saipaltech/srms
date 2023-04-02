package org.saipal.srms.vouchers;

import java.net.http.HttpRequest;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.saipal.srms.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ApiController {
	
	
	@Autowired
	TaxPayerVoucherService tp;
	
	@Autowired
	BankVoucherService bv;
	
	public String commKey="3543546841354sdfadfadf145a4df1dfas";
	
	/*
	 * To Be Called by SuTRA application, to get the voucher details 
	 * if they are already not pushed to the Sutra
	 * */
	@GetMapping("get-voucherbyno")
	public ResponseEntity<String> getVoucher(HttpServletRequest request) {
		String hv = request.getHeader("X-SECRET-KEY")!=null?request.getHeader("X-SECRET-KEY"):"";
		if(hv.equals(commKey)) {
			return  tp.getVoucherDetailsByVoucherNo();
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Invalid Request\"}");
	}
	
	/*
	 * To Be Called by SuTRA application, to get the deposit voucher details 
	 * using the payment reference number
	 * */
	@GetMapping("get-deposit-status")
	public ResponseEntity<Map<String, Object>> getVoucherStatus(HttpServletRequest request) {
		String hv = request.getHeader("X-SECRET-KEY")!=null?request.getHeader("X-SECRET-KEY"):"";
		if(hv.equals(commKey)) {
			return  bv.getVoucherStatus();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
		
	}
}
