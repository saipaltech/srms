package org.saipal.srms.report;

import java.util.Map;

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
	public ResponseEntity<Map<String, Object>>  getFys() {
		return rs.getFys();
	}
	
	@GetMapping("get-branches")
	public ResponseEntity<Map<String, Object>>  getBranches() {
		return rs.getBranches();
	}
	
	@GetMapping("get-llgs")
	public ResponseEntity<Map<String, Object>>  getLocalLevels() {
		return rs.getLocalLevels();
	}
}
