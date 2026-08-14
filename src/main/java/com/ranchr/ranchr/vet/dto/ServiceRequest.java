package com.ranchr.ranchr.vet.dto;

import jakarta.validation.constraints.NotNull;
import retrofit2.http.Url;

public record ServiceRequest(
        @NotNull
        String symptoms,
        String description,
        @NotNull
        String location,
        @NotNull
        String animalSpecies,
        @Url
        String animalPhotos
) {
}
