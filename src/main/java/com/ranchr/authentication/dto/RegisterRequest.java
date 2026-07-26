package com.ranchr.authentication.dto;

import jakarta.validation.constraints.Email;

public record RegisterRequest(
		@Email
		String 		email,
		String 		username,
		String 		password
) {
}
