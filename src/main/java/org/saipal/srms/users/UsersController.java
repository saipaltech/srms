package org.saipal.srms.users;

import java.util.List;
import java.util.Map;


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

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UsersController {
	
	@Autowired
	UsersService objService;

	@Autowired
	ValidationService validationService;

	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return objService.index();
	}

	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(Users.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
		} else {
			return objService.store();
		}
	}
	
	@PostMapping("/bank-user")
	public ResponseEntity<Map<String, Object>> storeBankUser(HttpServletRequest request) {
		Map<String,String> rules=Users.rules();
		rules.remove("branchid");
		rules.remove("approved");
		rules.remove("disabled");
		Validator validator = validationService.validate(rules);
		if (validator.isFailed()) {
			return Messenger.getMessenger().setMessage(validator.getErrorMessages()).error();
		} else {
			return objService.storeBankUser();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request, @PathVariable String id) {
		return objService.edit(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) {
		Validator validator = validationService.validate(Users.rules());
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
	
	
	@GetMapping("/get-user-details")
	public ResponseEntity<List<Map<String, Object>>> getUserDetails(HttpServletRequest request) {
		return objService.getUserList();
	}
	
	@GetMapping("/get-front-menu")
	public ResponseEntity<List<Map<String, Object>>> frontMenu() {
		return objService.frontMenu();
	}


}
