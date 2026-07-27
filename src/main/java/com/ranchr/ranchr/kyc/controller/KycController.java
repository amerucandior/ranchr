package com.ranchr.ranchr.kyc.controller;

import com.ranchr.ranchr.kyc.dto.KycRequest;
import com.ranchr.ranchr.kyc.dto.OtpRequest;
import com.ranchr.ranchr.kyc.otp.service.OtpService;
import com.ranchr.ranchr.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kyc")
@SecurityRequirement(name = "bearerAuth")
@Tag(
		name = "KYC",
		description = "Know-your-customer verification — phone/ID submission and OTP confirmation")
public class KycController {

	private final KycService kycService;

	@Operation(
			summary = "Request KYC OTP code",
			description = "Submits phone number (Kenyan format: +2547xxxxxxxx or +2541xxxxxxxx) and national ID (7-8 digits). " +
								  "OTP is sent via SMS if the phone/ID are not already registered to another user."
	)
	@ApiResponse(responseCode = "200", description = "OTP sent via SMS")
	@ApiResponse(responseCode = "409", description = "Phone number or national ID already registered")
	@PostMapping("/otprequest")
	public ResponseEntity<String> requestOtp(@RequestBody @Valid KycRequest request
			) {
		kycService.verify(request);
		return ResponseEntity.ok("Enter OTP code sent to your phone Number");
	}

	@Operation(
			summary = "Verify KYC OTP code",
			description = "Confirms the 6-digit OTP received via SMS. Completes KYC verification for the authenticated user."
	)
	@ApiResponse(responseCode = "200", description = "Phone number verified")
	@ApiResponse(responseCode = "401", description = "Invalid or expired OTP")	@PostMapping("/verify")
	public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpRequest request) {
		kycService.verifyOtp(request);
		return ResponseEntity.ok("Phone number verified");

	}
}
