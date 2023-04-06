package org.saipal.srms.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
	@Autowired
	RequestParserFilter requestParserFilter;
	
	CorsConfigurationSource corsConfig = new CorsConfigurationSource() {
		@Override
		public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
			CorsConfiguration configs = new CorsConfiguration().applyPermitDefaultValues();
			configs.addAllowedMethod(HttpMethod.PUT);
			configs.addAllowedMethod(HttpMethod.DELETE);
			configs.addAllowedOrigin(CorsConfiguration.ALL);
			return configs;
		}
	};
	
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
        		.cors(crs-> crs.configurationSource(corsConfig))
        		.csrf(csrf-> csrf.disable())
        		//.sessionManagement(ses->ses.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
        		.sessionManagement(ssn -> ssn.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        		.authorizeHttpRequests((reqs)->{
        			reqs
        			.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
        			.permitAll()
        			.antMatchers("/auth/login","/auth/2fa","/api/get-voucherbyno","/api/get-deposit-status")
        			.permitAll()
        			.anyRequest()
        			.authenticated()
        			.and()
        			.addFilterBefore(requestParserFilter, UsernamePasswordAuthenticationFilter.class)
    				.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        		})
                .formLogin(frm->frm.disable())
                .logout(lo -> lo
                		.logoutRequestMatcher(new AntPathRequestMatcher("/**/logout"))
                		.logoutSuccessUrl("/logout-done"))
                .httpBasic(htb->htb.disable())
				.build();
	}
}