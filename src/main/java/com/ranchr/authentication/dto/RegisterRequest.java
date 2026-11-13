package com.ranchr.authentication.dto;

import com.ranchr.authentication.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
		@Email
		@NotBlank
		String 		email,
		@NotBlank
		String 		username,
		@NotBlank
		String 		password,
		Role 		role
) {
	public RegisterRequest(String email, String username, String password) {
		this(email, username, password, Role.USER);
	}
}

