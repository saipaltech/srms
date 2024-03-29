package org.saipal.srms.users;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	@GetMapping("all")
	public ResponseEntity<Map<String, Object>> indexAll(HttpServletRequest request) {
		return objService.indexAll();
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
		Map<String, String> rules = Users.rules(); 
		rules.remove("username");
		rules.remove("password");
		Validator validator = validationService.validate(rules);
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
	@GetMapping("/get-usertypes")
	public ResponseEntity<List<Map<String, Object>>> getUerTypes() {
		return objService.getUerTypes();
	}
	
	@GetMapping("/get-front-menu")
	public ResponseEntity<Object> frontMenu() {
		return objService.frontMenu();
	}
	
	@PostMapping("reset-password/{id}")
	public ResponseEntity<Map<String, Object>> resetPassword(HttpServletRequest request, @PathVariable String id) {
		return objService.resetPassword(id);

	}
	
	@PostMapping("change-password")
	public ResponseEntity<Map<String, Object>> changePassword(HttpServletRequest request) {
		
			return objService.changePassword();
	}
	
	@PostMapping("change-password-login")
	public ResponseEntity<Map<String, Object>> changePasswordLogin(HttpServletRequest request) {
			return objService.changePasswordLogin();
	}

	@PostMapping("reset-passbypin")
	public ResponseEntity<Map<String, Object>> resetPassByPin(HttpServletRequest request) {
			return objService.resetPassByPin();
	}
	
	@PostMapping("upload-users")
	public ResponseEntity<Map<String, Object>> uploadUsers(@RequestParam("file") MultipartFile file) {
			return objService.uploadUsers(file);
	}
	
	@GetMapping("download-excel")
	public void downloadExcel(HttpServletResponse resp) {
		Workbook wb = objService.downloadImportFile();
		String fileName = "users_import.xlsx";
		resp.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		try {
			wb.write(resp.getOutputStream());
			wb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
