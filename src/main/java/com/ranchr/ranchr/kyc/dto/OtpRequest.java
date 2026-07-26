package com.ranchr.ranchr.kyc.dto;

import jakarta.validation.constraints.Size;

public record OtpRequest(
		@Size(max = 6, min = 6)
		String code) {
}
