package com.ranchr.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
	private String secretKey;

	@Value("${security.jwt.access-token-expiry}")
	private long accessTokenExpiry;

	@Value("${security.jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;

	// Token Generation
	public String generateAccessToken(UserDetails userDetails) {
		List<String> roles = userDetails.getAuthorities().stream()
									 .map(GrantedAuthority::getAuthority)
									 .toList();
		return buildToken(Map.of("roles", roles), userDetails.getUsername(), accessTokenExpiry);
	}

	public String generateAccessToken(String username, List<String> roles) {
		return buildToken(Map.of("roles", roles), username, accessTokenExpiry);
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return buildToken(Map.of(), userDetails.getUsername(), refreshTokenExpiry);
	}

	public String generateRefreshToken(String username) {
		return buildToken(Map.of(), username, refreshTokenExpiry);
	}

	private String buildToken(Map<String, Object> claims,
	                          String subject,
	                          long expiry) {
		Instant now = Instant.now();
		return Jwts.builder()
					   .claims(claims)
					   .subject(subject)
					   .issuedAt(Date.from(now))
					   .expiration(Date.from(now.plusMillis(expiry)))
					   .signWith(getSigningKey())
					   .compact();
	}

	// token validation

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	// claims Extraction
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
					   .verifyWith(getSigningKey())
					   .build()
					   .parseSignedClaims(token)
					   .getPayload();
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}
}
