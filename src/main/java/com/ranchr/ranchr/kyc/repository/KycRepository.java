package com.ranchr.ranchr.kyc.repository;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.kyc.model.Kyc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KycRepository extends JpaRepository<Kyc, UUID> {

	Optional<Kyc> findByPhoneNumber(String phoneNumber);
	Optional<Kyc> findByNationalId(String nationalId);
	Boolean existsByPhoneNumber(String phoneNumber);
	Boolean existsByNationalId(String nationalId);
	Optional<Kyc> findByUser(User user);

}
