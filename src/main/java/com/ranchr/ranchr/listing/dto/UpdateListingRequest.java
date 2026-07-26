package com.ranchr.ranchr.listing.dto;

import com.ranchr.ranchr.listing.models.enums.Gender;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Full-replace (PUT) semantics: every field here overwrites the current value.
 * {@code status} and {@code media} are the exceptions — {@code null} means
 * "leave unchanged" rather than "clear it", since those aren't things a client
 * normally resubmits on every edit.
 */
public record UpdateListingRequest(
		@NotBlank String animalType,
		Gender gender,
		@PositiveOrZero Integer ageYears,
		@PositiveOrZero Integer ageMonths,
		String breed,
		String description,
		@NotBlank String location,
		@NotNull ListingServiceType serviceType,
		ListingStatus status,
		@NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal fee,
		LocalDate availableFrom,
		LocalDate availableTo
) {
}