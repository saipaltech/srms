package org.saipal.srms.vouchers;

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
	
	@GetMapping("get-voucherno")
	public ResponseEntity<String> getVoucher() {
		return  tp.getVoucherDetailsByVoucherNo();
	}
}
