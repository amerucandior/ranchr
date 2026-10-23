package com.ranchr.ranchr.vet.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateServiceRequestDto(
        UUID targetVetId,
        @NotBlank(message = "Animal species is required (e.g. Cow, Goat, Sheep)")
        String animalSpecies,
        @NotBlank(message = "Symptoms or reason for visit is required")
        String symptoms,
        String description,
        @NotBlank(message = "Location or farm address is required")
        String location,
        String animalPhotos,
        boolean isHeatAlert
) {
}
