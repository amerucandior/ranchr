package com.ranchr.authentication.service;

import com.ranchr.authentication.dto.AuthResponse;
import com.ranchr.authentication.dto.LoginRequest;
import com.ranchr.authentication.dto.RegisterRequest;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.authentication.security.JwtService;
import com.ranchr.exceptions.DuplicateUserException;
import com.ranchr.exceptions.InvalidCredentialsException;
import com.ranchr.exceptions.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository 	userRepository;
	private final PasswordEncoder 	passwordEncoder;
	private final JwtService		jwtService;
	private final AuthenticationManager	authManager;
	private final UserDetailsService	userDetailsService;

	public AuthResponse register (RegisterRequest request) {

		// check for duplicate username/email before persisting
		if (userRepository.existsByUsername(request.username())) {
			throw new DuplicateUserException("Username Already Taken");
		}

		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateUserException("Email already registered");
		}

		User user = User.builder()
							.username(request.username())
							.email(request.email())
							.password(passwordEncoder.encode(request.password()))
							.role(Role.USER)
							.build();

		userRepository.save(user);
		String role = "ROLE_" + user.getRole().name();
		return new AuthResponse(
				jwtService.generateAccessToken(user.getUsername(), List.of(role)),
				jwtService.generateRefreshToken(user.getUsername())
		);	}


	public AuthResponse login(LoginRequest request) {
		// ✅ Delegates to DaoAuthenticationProvider — handles bad credentials
		authManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.username(),
						request.password()
				)
		);

		User user = userRepository.findByUsername(request.username())
							.orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

		return buildAuthResponse(user);
	}

	public AuthResponse refresh(String refreshToken) {
		String username;
		try {
			username = jwtService.extractUsername(refreshToken);
		} catch (Exception e) {
			throw new InvalidTokenException("Invalid or expired refresh token");
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);

		if (!jwtService.isTokenValid(refreshToken, userDetails)) {
			throw new InvalidTokenException("Invalid or expired refresh token");
		}

		return new AuthResponse(
				jwtService.generateAccessToken(userDetails),
				jwtService.generateRefreshToken(userDetails)
		);
	}

	private AuthResponse buildAuthResponse(User user) {
		// UserDetails adapter for JwtService
		var userDetails = org.springframework.security.core.userdetails.User
								  .withUsername(user.getUsername())
								  .password(user.getPassword())
								  .authorities("ROLE_" + user.getRole().name())
								  .build();

		return new AuthResponse(
				jwtService.generateAccessToken(userDetails),
				jwtService.generateRefreshToken(userDetails)
		);
	}
}
