package com.ranchr.ranchr.vet.dto;

import com.ranchr.ranchr.vet.model.VetRating;

import java.time.Instant;
import java.util.UUID;

public record VetRatingResponseDto(
        UUID id,
        UUID vetId,
        String vetName,
        UUID ratedByUserId,
        String ratedByUsername,
        UUID serviceId,
        Integer rating,
        String comment,
        Instant createdAt
) {
    public static VetRatingResponseDto from(VetRating r) {
        return new VetRatingResponseDto(
                r.getId(),
                r.getVetProfile() != null ? r.getVetProfile().getVetId() : null,
                r.getVetProfile() != null ? r.getVetProfile().getKvbRegisteredName() : null,
                r.getRatedBy() != null ? r.getRatedBy().getUserId() : null,
                r.getRatedBy() != null ? r.getRatedBy().getUsername() : null,
                r.getService() != null ? r.getService().getServiceId() : null,
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
