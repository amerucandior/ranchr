package com.ranchr.ranchr.vet.dto;

import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;

import java.time.Instant;
import java.util.UUID;

public record ServiceRequestResponseDto(
        UUID serviceId,
        UUID farmerId,
        String farmerUsername,
        String farmerPhone,
        UUID vetId,
        String vetName,
        String vetPhone,
        String animalSpecies,
        String symptoms,
        String description,
        String location,
        String animalPhotos,
        ServiceStatus status,
        Instant createdAt,
        Instant acceptedAt,
        Instant completedAt
) {
    public static ServiceRequestResponseDto from(ServiceRequest req) {
        return from(req, null, null);
    }

    public static ServiceRequestResponseDto from(ServiceRequest req, String farmerPhone, String vetPhone) {
        UUID vetId = null;
        String vetName = null;

        if (req.getVetProfile() != null) {
            vetId = req.getVetProfile().getVetId();
            vetName = req.getVetProfile().getKvbRegisteredName();
        }

        return new ServiceRequestResponseDto(
                req.getServiceId(),
                req.getFarmer() != null ? req.getFarmer().getUserId() : null,
                req.getFarmer() != null ? req.getFarmer().getUsername() : null,
                farmerPhone,
                vetId,
                vetName,
                vetPhone,
                req.getAnimalSpecies(),
                req.getSymptoms(),
                req.getDescription(),
                req.getLocation(),
                req.getAnimalPhotos(),
                req.getStatus(),
                req.getCreatedAt(),
                req.getAcceptedAt(),
                req.getCompletedAt()
        );
    }
}
