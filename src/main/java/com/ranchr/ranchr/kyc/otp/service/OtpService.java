package com.ranchr.ranchr.kyc.otp.service;

import com.ranchr.ranchr.kyc.model.Kyc;

public interface OtpService {
	String generateAndStoreOtp(Kyc kyc);
	void validateAndClear(Kyc kyc, String rawOtp);
}
