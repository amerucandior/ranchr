package com.ranchr.ranchr.vet.dto;

import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.ServicesOffered;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record VetProfileResponseDto(
        UUID vetId,
        String kvbRegisteredName,
        String kvbLicenseNumber,
        Integer yearsExperience,
        Set<ServicesOffered> servicesOffered,
        Available available,
        VerificationStatus status,
        LocalDateTime verifiedAt
) {
    public static VetProfileResponseDto from(VetProfile profile) {
        return new VetProfileResponseDto(
                profile.getVetId(),
                profile.getKvbRegisteredName(),
                profile.getKvbLicenseNumber(),
                profile.getYearsExperience(),
                profile.getServicesOffered(),
                profile.getAvailable(),
                profile.getStatus(),
                profile.getVerifiedAt()
        );
    }
}
