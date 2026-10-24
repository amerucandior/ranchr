package com.ranchr.ranchr.reproduction.dto;

import com.ranchr.ranchr.reproduction.model.ReproductiveCycle;
import com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ReproductiveCycleResponseDto(
        UUID id,
        UUID serviceId,
        UUID listingId,
        UUID farmerId,
        String farmerUsername,
        UUID vetId,
        String vetName,
        String animalIdentifier,
        String animalSpecies,
        String semenStrawBatchNumber,
        LocalDate inseminationDate,
        LocalDate nonReturnCheckDate,
        LocalDate pregnancyDiagnosisDate,
        LocalDate expectedCalvingDate,
        ReproductiveStatus status,
        long daysUntilNonReturn,
        long daysUntilPregnancyDiagnosis,
        long daysUntilCalving,
        boolean day21AlertSent,
        boolean day60AlertSent,
        boolean calvingAlertSent,
        String notes,
        Instant createdAt
) {
    public static ReproductiveCycleResponseDto from(ReproductiveCycle cycle) {
        LocalDate today = LocalDate.now();

        long daysUntilNonReturn = cycle.getNonReturnCheckDate() != null
                ? ChronoUnit.DAYS.between(today, cycle.getNonReturnCheckDate())
                : 0;
        long daysUntilPD = cycle.getPregnancyDiagnosisDate() != null
                ? ChronoUnit.DAYS.between(today, cycle.getPregnancyDiagnosisDate())
                : 0;
        long daysUntilCalving = cycle.getExpectedCalvingDate() != null
                ? ChronoUnit.DAYS.between(today, cycle.getExpectedCalvingDate())
                : 0;

        UUID serviceId = cycle.getServiceRequest() != null ? cycle.getServiceRequest().getServiceId() : null;
        UUID listingId = cycle.getListing() != null ? cycle.getListing().getId() : null;
        UUID vetId = cycle.getVetProfile() != null ? cycle.getVetProfile().getVetId() : null;
        String vetName = cycle.getVetProfile() != null ? cycle.getVetProfile().getKvbRegisteredName() : null;

        return new ReproductiveCycleResponseDto(
                cycle.getId(),
                serviceId,
                listingId,
                cycle.getFarmer().getUserId(),
                cycle.getFarmer().getUsername(),
                vetId,
                vetName,
                cycle.getAnimalIdentifier(),
                cycle.getAnimalSpecies(),
                cycle.getSemenStrawBatchNumber(),
                cycle.getInseminationDate(),
                cycle.getNonReturnCheckDate(),
                cycle.getPregnancyDiagnosisDate(),
                cycle.getExpectedCalvingDate(),
                cycle.getStatus(),
                daysUntilNonReturn,
                daysUntilPD,
                daysUntilCalving,
                cycle.isDay21AlertSent(),
                cycle.isDay60AlertSent(),
                cycle.isCalvingAlertSent(),
                cycle.getNotes(),
                cycle.getCreatedAt()
        );
    }
}
