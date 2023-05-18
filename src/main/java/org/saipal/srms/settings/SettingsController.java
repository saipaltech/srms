package org.saipal.srms.settings;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings")
public class SettingsController {
	@Autowired
	SettingsService objService;
	
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> OtpSettings() {
			return objService.updateSettings();
	}
}
