package com.ranchr.ranchr.kyc.controller;

import com.ranchr.ranchr.kyc.dto.KycRequest;
import com.ranchr.ranchr.kyc.dto.OtpRequest;
import com.ranchr.ranchr.kyc.otp.service.OtpService;
import com.ranchr.ranchr.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
		name = "KYC",
		description = "user verification"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/kyc")
public class KycController {

	private final KycService kycService;

	@Operation(summary = "request otp code for verification")
	@PostMapping("/otprequest")
	public ResponseEntity<String> requestOtp(@RequestBody @Valid KycRequest request
			) {
		kycService.verify(request);
		return ResponseEntity.ok("Enter OTP code sent to your phone Number");
	}

	@Operation(summary = "verify Otp Code")
	@PostMapping("/verify")
	public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpRequest request) {
		kycService.verifyOtp(request);
		return ResponseEntity.ok("Phone number verified");

	}
}
