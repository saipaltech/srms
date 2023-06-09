package org.saipal.srms.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	AuthService authService;
	
	@PostMapping("login")
	public ResponseEntity<Map<String, Object>> login() {
		return authService.checkUser();
	}
	
	@GetMapping("re-login")
	public ResponseEntity<Map<String, Object>> reLogin() {
		return authService.reLogin();
	}
	
	@PostMapping("user-login")
	public ResponseEntity<Map<String, Object>> loginUser() {
		return authService.loginUser();
	}
	
	@PostMapping("2fa")
	public ResponseEntity<Map<String, Object>> twoFa() {
		return authService.twoFa();
	}
	
	@PostMapping("api-login")
	public ResponseEntity<Map<String, Object>> apiLogin() {
		return authService.apiLogin();
	}
	
	@PostMapping("get-pincode")
	public ResponseEntity<Map<String, Object>> getPincode() {
		return authService.getPincode();
	}
}
