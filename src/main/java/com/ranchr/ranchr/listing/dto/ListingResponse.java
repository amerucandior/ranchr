package com.ranchr.ranchr.listing.dto;

import com.ranchr.ranchr.listing.models.enums.Gender;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ListingResponse(
		UUID id,
		UUID ownerId,
		String animalType,
		Gender gender,
		Integer ageYears,
		Integer ageMonths,
		String breed,
		String description,
		String location,
		ListingServiceType serviceType,
		ListingStatus status,
		BigDecimal fee,
		LocalDate availableFrom,
		LocalDate availableTo,
		int interestCount,
		List<ListingMediaResponse> images,
		Instant createdAt,
		Instant updatedAt
){
}
