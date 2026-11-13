package com.ranchr.ranchr.kyc.dto;

import java.time.LocalDateTime;

public record KycStatusDto(
		boolean verified,
		String phoneNumber,
		String nationalId,
		LocalDateTime verifiedAt
) {
}
