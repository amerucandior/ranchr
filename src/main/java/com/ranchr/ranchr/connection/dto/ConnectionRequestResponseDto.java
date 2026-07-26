package com.ranchr.ranchr.connection.dto;

import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;

import java.time.Instant;
import java.util.UUID;

public record ConnectionRequestResponseDto(

		UUID id, UUID listingId, UUID interestedUserId,
		ConnectionStatus status, String message, Instant createdAt
) {
	public static ConnectionRequestResponseDto from(ConnectionRequest r) {
		return new ConnectionRequestResponseDto(
				r.getId(), r.getListing().getId(), r.getInterestedUser().getId(),
				r.getStatus(), r.getMessage(), r.getCreatedAt());
	}
}