package org.saipal.srms.dayclosecheque;

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
@RequestMapping("/dayclosecheque")
public class DayCloseChequeController {
	@Autowired
	DaycloseChequeService objService;

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
