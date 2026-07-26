package com.ranchr.ranchr.kyc.otp.serviceimpl;

import com.ranchr.exceptions.InvalidOtpException;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.otp.service.OtpService;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

	private static final int    OTP_DIGITS         = 6;
	private static final long   OTP_EXPIRY_SECONDS = 5 * 60L;   // 5 minutes
	private static final int    OTP_UPPER_BOUND    = 1_000_000;  // 10^6 → 000000–999999
	private static final SecureRandom RANDOM       = new SecureRandom();

	private final KycRepository kycRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public String generateAndStoreOtp(Kyc kyc) {
		String plain = String.format("%0" + OTP_DIGITS + "d", RANDOM.nextInt(OTP_UPPER_BOUND));

		kyc.setOtpCode(passwordEncoder.encode(plain));
		kyc.setOtpExpiry(Instant.now().plusSeconds(OTP_EXPIRY_SECONDS));
		kycRepository.save(kyc);

		return plain;
	}

	@Override
	public void validateAndClear(Kyc kyc, String rawOtp) {

		if (kyc.getOtpCode() == null || kyc.getOtpExpiry() == null) {
			throw new InvalidOtpException("No Otp pending for this account");
		}

		if (Instant.now().isAfter(kyc.getOtpExpiry())) {
			clearOtp(kyc);
			throw new InvalidOtpException("Otp has expired");
		}

		if (!passwordEncoder.matches(rawOtp, kyc.getOtpCode())) {
			throw new InvalidOtpException("Invalid OTP");
		}

		clearOtp(kyc);
	}

	private void clearOtp(Kyc kyc) {
		kyc.setOtpCode(null);
		kyc.setOtpExpiry(null);
		kycRepository.save(kyc);
	}
}
