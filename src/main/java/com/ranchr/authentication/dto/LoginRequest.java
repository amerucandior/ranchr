package com.ranchr.authentication.dto;

public record LoginRequest(
		String username,
		String password
) {
}
