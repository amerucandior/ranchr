package com.ranchr.authentication.controller;

import com.ranchr.authentication.dto.AuthResponse;
import com.ranchr.authentication.dto.LoginRequest;
import com.ranchr.authentication.dto.RefreshRequest;
import com.ranchr.authentication.dto.RegisterRequest;
import com.ranchr.authentication.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(
		name = "Authentication",
		description = "Endpoints for user authentication and authorization"
)
public class AuthController {

	private final AuthService authService;

	@Operation(
			summary = "Register a new user",
			description = "Creates a new user account and returns JWT access and refresh tokens. " +
								  "Email must be valid format. Username must be unique."
	)
	@ApiResponse(responseCode = "201", description = "User created, tokens returned")
	@ApiResponse(responseCode = "409", description = "Username or email already taken")
	@PostMapping("register")
	public ResponseEntity<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@Operation(
			summary = "Login",
			description = "Authenticates with username and password. Returns JWT access and refresh tokens. " +
								  "Error message is intentionally generic: 'Invalid username or password'."
	)
	@ApiResponse(responseCode = "200", description = "Authenticated, tokens returned")
	@ApiResponse(responseCode = "401", description = "Invalid username or password")
	@PostMapping("login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request
			) {
		return ResponseEntity.ok(authService.login(request));
	}

	@Operation(
			summary = "Refresh access token",
			description = "Generates a new access/refresh token pair from a valid refresh token. " +
								  "Access token TTL: 15 min. Refresh token TTL: 7 days."
	)
	@ApiResponse(responseCode = "200", description = "New token pair returned")
	@ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
	@PostMapping("refresh")
	public ResponseEntity<AuthResponse> refresh(
			@Valid @RequestBody RefreshRequest request
	) {
		return ResponseEntity.ok(authService.refresh(request.refreshToken()));
	}
}
