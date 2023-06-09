package org.saipal.srms.config;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import org.saipal.srms.auth.Authenticated;
import org.saipal.srms.parser.RequestParser;
import org.saipal.srms.util.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	Authenticated auth;

	@Autowired
	RequestParser doc;

	@Autowired
	JwtHelper jwtHelper;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		String appUri = requestURI.replace(contextPath, "");
		boolean staticMatcher = PathRequest.toStaticResources().atCommonLocations().matches(request);
		if(staticMatcher) {
			return true;
		}
		if(appUri.startsWith("/auth/login")||
				appUri.startsWith("/auth/2fa") || 
				appUri.startsWith("/auth/api-login") ||
				appUri.startsWith("/web") ||
				appUri.startsWith("/users/change-password-login") ||
				appUri.startsWith("/taxpayer-voucher/dayclose-details")||
				appUri.startsWith("/auth/get-pincode") ||
				appUri.startsWith("/users/reset-passbypin") ||
				appUri.startsWith("/dayclose/scheduler")
				) {
			return true;
		}
		return false; 
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String jwtToken = getTokenfromRequest(request);
		if (jwtToken != null) {
			/*
			 * @Author: Lifecracker87 Accesses token from request header Validates the token
			 * Set authorized data to a Authrepo, which is accessable from everywhere in the
			 * project using Authenticated servcice
			 */
			try {
				String sub = jwtHelper.getSubject(jwtToken);
				auth.setUserId(sub);
				auth.initSession();
				UsernamePasswordAuthenticationToken springAuthToken = new UsernamePasswordAuthenticationToken(sub, null,
						new ArrayList<>());
				SecurityContextHolder.getContext().setAuthentication(springAuthToken);
			} catch (ExpiredJwtException e) {
				setHeaderForEx(response, 0, "Token expired");
				return;
			} catch (UnsupportedJwtException e) {
				setHeaderForEx(response, 0, "Unsupported Token");
				return;
			} catch (MalformedJwtException e) {
				setHeaderForEx(response, 0, "Malformed Token");
				return;
			} catch (IllegalArgumentException e) {
				setHeaderForEx(response, 0, "Illigal Token");
				return;
			}
		}
		filterChain.doFilter(request, response);

	}

	public String getTokenfromRequest(HttpServletRequest request) {
		if (request.getHeader("Authorization") == null) {
			if (doc.getElementById("_token").getValue().isBlank()) {
				return null;
			}
			return doc.getElementById("_token").getValue();
		}
		return request.getHeader("Authorization").replace("Bearer ", "");
	}

	public void setHeaderForEx(HttpServletResponse response, int code, String mesage) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getOutputStream().print(message(code, mesage));

	}

	private String message(int code, String mesage) {
		return "{\"status\":" + code + ",\"message\":\"" + mesage + "\"}";
	}

}
