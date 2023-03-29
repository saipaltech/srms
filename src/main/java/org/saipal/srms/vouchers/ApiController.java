package org.saipal.srms.vouchers;

import java.net.http.HttpRequest;
import java.util.Map;

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
	
	@GetMapping("get-voucherbyno")
	public ResponseEntity<String> getVoucher(HttpRequest request) {
		String hv = request.headers().firstValue("X-SECRET-KEY")!=null?request.headers().firstValue("X-SECRET-KEY").get():"";
		if(hv.equals(commKey)) {
			return  tp.getVoucherDetailsByVoucherNo();
		}
		return ResponseEntity.ok("{\"status\":0,\"message\":\"Invalid Request\"}");
	}
	
	@GetMapping("get-deposit-status")
	public ResponseEntity<Map<String, Object>> getVoucherStatus(HttpRequest request) {
		String hv = request.headers().firstValue("X-SECRET-KEY")!=null?request.headers().firstValue("X-SECRET-KEY").get():"";
		if(hv.equals(commKey)) {
			return  bv.getVoucherStatus();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
		
	}
}
