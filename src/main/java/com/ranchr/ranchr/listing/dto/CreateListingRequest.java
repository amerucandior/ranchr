package com.ranchr.ranchr.listing.dto;

import com.ranchr.ranchr.listing.models.enums.Gender;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateListingRequest(
		@NotBlank String animalType,
		Gender gender,
		@PositiveOrZero Integer ageYears,
		@PositiveOrZero Integer ageMonths,
		String breed,
		String description,
		@NotBlank String location,
		@NotNull ListingServiceType serviceType,
		@NotNull @DecimalMin(value = "0.0") BigDecimal fee,
		LocalDate availableFrom,
		LocalDate availableTo
) {
}