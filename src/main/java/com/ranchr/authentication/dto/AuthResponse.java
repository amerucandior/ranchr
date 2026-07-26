package com.ranchr.authentication.dto;

public record AuthResponse(
		String accessToken,
		String refreshToken
) {
}
