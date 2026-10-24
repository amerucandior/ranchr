package com.ranchr.ranchr.reproduction.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReproductiveCycleDto(
        UUID serviceId,
        UUID listingId,
        UUID vetId,
        @NotBlank(message = "Animal identifier (name or ear tag) is required")
        String animalIdentifier,
        @NotBlank(message = "Animal species is required (e.g. Cattle, Goat, Sheep)")
        String animalSpecies,
        String semenStrawBatchNumber,
        LocalDate inseminationDate,
        String notes
) {
}
