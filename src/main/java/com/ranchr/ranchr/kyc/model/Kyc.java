package com.ranchr.ranchr.kyc.model;

import com.ranchr.authentication.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc")
@Builder
@Data
public class Kyc {

	@Id
	private UUID kycId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "id")
	private User user;

	@Column(name = "phone_number", unique = true)
	private String phoneNumber;

	@Column(name = "national_id", unique = true)
	private String nationalId;

	private String otpCode;

	@Column(name = "otp_expiry")
	private Instant otpExpiry;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private boolean verified = false;

	private LocalDateTime verifiedAt;
}
