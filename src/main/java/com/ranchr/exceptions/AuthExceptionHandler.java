package com.ranchr.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class AuthExceptionHandler {

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ApiError> handleInvalidToken(InvalidTokenException ex) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<ApiError> handleDuplicateUser(DuplicateUserException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
	public ResponseEntity<ApiError> handleInvalidCredentials(RuntimeException ex) {
		// don't echo ex.getMessage() here — Spring Security's BadCredentialsException
		// message can vary by provider and shouldn't leak auth internals to the client
		return build(HttpStatus.UNAUTHORIZED, "Invalid username or password");
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String message) {
		ApiError body = new ApiError(status.value(), message, Instant.now());
		return ResponseEntity.status(status).body(body);
	}
}
