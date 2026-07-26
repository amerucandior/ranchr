package com.ranchr.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;


@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex);
	}

	@ExceptionHandler(ListingNotFoundException.class)
	public ResponseEntity<ApiError> handleListingNotFound(ListingNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
		return build(HttpStatus.CONFLICT, ex);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
		return build(HttpStatus.BAD_REQUEST, ex);
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex) {
		return build(HttpStatus.FORBIDDEN, ex);
	}

	@ExceptionHandler(ListingAccessDeniedException.class)
	public ResponseEntity<ApiError> handleListingAccessDenied(ListingAccessDeniedException ex) {
		return build(HttpStatus.FORBIDDEN, ex);
	}

	@ExceptionHandler(CloudinaryUploadException.class)
	public ResponseEntity<ApiError> handleCloudinaryUpload(CloudinaryUploadException ex) {
		// 502: we're a gateway to Cloudinary, and it failed us — log full details, don't leak them to the client
		log.error("Cloudinary upload failed", ex);
		return build(HttpStatus.BAD_GATEWAY, ex);
	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<ApiError> handleInvalidOtp(InvalidOtpException ex) {
		return build(HttpStatus.UNAUTHORIZED, ex);
	}

	@ExceptionHandler(SmsDeliveryException.class)
	public ResponseEntity<ApiError> handleSmsDeliveryFailure(SmsDeliveryException ex) {
		// 502: we're a gateway to
		// Africa's Talking SMS, and it failed us, so don't blame the client
		log.error("SMS delivery failed", ex);
		return build(HttpStatus.BAD_GATEWAY, ex);
	}

	// keep this LAST — catch-all fallback stays at 500 for genuinely unexpected exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
		log.error("Unhandled exception", ex);
		ApiError body = new ApiError(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal server error",
				Instant.now()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	private ResponseEntity<ApiError> build(HttpStatus status, Exception ex) {
		ApiError body = new ApiError(status.value(), ex.getMessage(), Instant.now());
		return ResponseEntity.status(status).body(body);
	}
}
