package org.saipal.srms.dayclose;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.saipal.srms.util.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dayclose")
public class DayCloseController {
	@Autowired
	DaycloseService objService;

	@Autowired
	ValidationService validationService;
	@PostMapping("getdayclose")
	public ResponseEntity<Map<String, Object>> getdayclose(HttpServletRequest request) throws JSONException {
		
			return objService.getdayclose();
		
	}
	@PostMapping("submitdayclose")
	public ResponseEntity<Map<String, Object>> submitdayclose(HttpServletRequest request) throws JSONException {
		
			return objService.submitdayclose();
		
	}
}
