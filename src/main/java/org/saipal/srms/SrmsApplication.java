package org.saipal.srms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication
public class SrmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SrmsApplication.class, args);
	}

}
