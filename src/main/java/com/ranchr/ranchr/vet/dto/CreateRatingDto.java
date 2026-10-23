package com.ranchr.ranchr.vet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRatingDto(
        @NotNull(message = "Rating score is required")
        @Min(value = 1, message = "Minimum rating is 1")
        @Max(value = 5, message = "Maximum rating is 5")
        Integer rating,

        @Size(max = 250, message = "Comment must not exceed 250 characters")
        String comment
) {
}
