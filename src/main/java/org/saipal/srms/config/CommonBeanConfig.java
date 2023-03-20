package org.saipal.srms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.pebbletemplates.pebble.extension.Extension;

@Configuration
public class CommonBeanConfig {
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	AuthenticationManager authenticationManagerBean() {
	    return null;
	}
	
	@Bean
	Extension fmisPebbleExtension() {
		return new PebbleExtension();
	}
}
