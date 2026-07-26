package com.ranchr.ranchr.listing.dto;

import java.util.UUID;

public record ListingMediaResponse(
		UUID id,
		String url,
		String publicId,
		Integer sortOrder
) {
}