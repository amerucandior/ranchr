package com.ranchr.ranchr.kyc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record KycRequest(

		@NotBlank(message = "Phone number is required")
		@Pattern(
				regexp = "^\\+254(?:7\\d{8}|1\\d{8})$",
				message = "Phone number must be a valid Kenyan number starting with +2547 or +2541"
		)
		String phoneNumber,

		@NotBlank(message = "national Id required")
		@Size(min = 7, max = 8)
		String nationalId
){
}
