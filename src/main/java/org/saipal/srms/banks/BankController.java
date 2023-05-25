package org.saipal.srms.banks;


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
@RequestMapping("/bank")
public class BankController {
	
	@Autowired
	BankService objService;

	@Autowired
	ValidationService validationService;
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return objService.index();
	}

	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(Bank.rules());
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
		Validator validator = validationService.validate(Bank.rules());
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
	
	@GetMapping("/banks-from-sutra")
	public ResponseEntity<List<Map<String, Object>>> getBanksFromSutra() {
		return objService.getBanksFromSutra();
	}
	
	@GetMapping("/getDistrict")
	public ResponseEntity<List<Map<String, Object>>> getDistrict() {
		return objService.getDistrict();
	}
	
	@GetMapping("/getPalika")
	public ResponseEntity<List<Map<String, Object>>> getPalika() {
		return objService.getPalika();
	}
	
	@GetMapping("/getPalikaAll")
	public ResponseEntity<List<Map<String, Object>>> getPalikaAll() {
		return objService.getPalikaAll();
	}
}
