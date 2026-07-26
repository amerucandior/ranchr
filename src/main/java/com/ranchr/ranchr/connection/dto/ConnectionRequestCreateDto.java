package com.ranchr.ranchr.connection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConnectionRequestCreateDto(
		@NotNull UUID listingId,
		@Size(max = 1000) String message
) {
}
