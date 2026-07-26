package com.ranchr.ranchr.kyc.serviceimpl;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.exceptions.DuplicateUserException;
import com.ranchr.ranchr.kyc.dto.KycRequest;
import com.ranchr.ranchr.kyc.dto.OtpRequest;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.otp.service.OtpService;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.kyc.service.KycService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

	private final SmsService smsService;
	private final KycRepository kycRepository;
	private final OtpService otpService;
	private final CurrentUser currentUser;


	@Override
	@Transactional
	public void verify(KycRequest request) {

		User user = currentUser.get();

		Optional<Kyc> existing = kycRepository.findByUser(user);

		if (existing.isPresent() && existing.get().isVerified()) {
			throw new DuplicateUserException("User already verified");
		}

		Optional<Kyc> byNationalId = kycRepository.findByNationalId(request.nationalId());
		if (byNationalId.isPresent() && !byNationalId.get().getUser().getUserId().equals(user.getUserId())) {
			throw new DuplicateUserException("National ID already in use");
		}

		Optional<Kyc> byPhone = kycRepository.findByPhoneNumber(request.phoneNumber());
		if (byPhone.isPresent() && !byPhone.get().getUser().getUserId().equals(user.getUserId())) {
			throw new DuplicateUserException("Phone Number already in use");
		}

		Kyc kyc = existing.orElseGet(() -> Kyc.builder().user(user).build());
		kyc.setPhoneNumber(request.phoneNumber());
		kyc.setNationalId(request.nationalId());
		kycRepository.save(kyc);

		String code = otpService.generateAndStoreOtp(kyc);
		smsService.send(request.phoneNumber(),
				"Your verification code is %s. It expires in 5 minutes. Do not share this code with anyone."
						.formatted(code));
	}

	@Transactional
	@Override
	public void verifyOtp(OtpRequest request) {

		User user = currentUser.get();

		Kyc kyc = kycRepository.findByUser(user)
						  .orElseThrow(() -> new UsernameNotFoundException(
								  "No pending verification for this account"));


		otpService.validateAndClear(kyc, request.code());

		kyc.setVerified(true);
		kyc.setVerifiedAt(LocalDateTime.now());
		kycRepository.save(kyc);
	}

}
