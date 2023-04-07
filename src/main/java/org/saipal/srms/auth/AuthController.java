package org.saipal.srms.auth;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
	public ResponseEntity<Map<String, Object>> login(HttpServletRequest request) {
		return authService.checkUser();
	}
	
	@GetMapping("re-login")
	public ResponseEntity<Map<String, Object>> reLogin(HttpServletRequest request) {
		return authService.reLogin();
	}
	
	@PostMapping("2fa")
	public ResponseEntity<Map<String, Object>> twoFa(HttpServletRequest request) {
		return authService.twoFa();
	}
}
