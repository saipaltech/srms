package org.saipal.srms.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtHelper {
	
	@Value("${jwt.key}")
	private String jwtSecretKey;

	public String createToken(String subject) {
		return Jwts.builder().setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
				.signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes())).compact();

	}

	public String getSubject(String token) {
		return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecretKey.getBytes())).build()
				.parseClaimsJws(token).getBody().getSubject();
	}
	
	public boolean isExpired(String token) {
		Date expTime = Jwts.parserBuilder().build().parseClaimsJws(token).getBody().getExpiration();
		Date curDate = new Date(System.currentTimeMillis()-(1000*20));
		if(expTime.after(curDate)) {
			return false;
		}
		return true;
	}
	
	
}
