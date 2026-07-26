package com.ranchr.ranchr.kyc.service;

import com.ranchr.ranchr.kyc.dto.KycRequest;
import com.ranchr.ranchr.kyc.dto.OtpRequest;

public interface KycService {
	void verify(KycRequest request);
	void verifyOtp(OtpRequest request);
}
