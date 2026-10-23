package com.ranchr.ranchr.connection.dto;

import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;

import java.time.Instant;
import java.util.UUID;

public record ConnectionRequestResponseDto(
		UUID id,
		UUID listingId,
		UUID interestedUserId,
		String interestedUsername,
		String ownerUsername,
		ConnectionStatus status,
		String message,
		Instant createdAt
) {
	public static ConnectionRequestResponseDto from(ConnectionRequest r) {
		String interestedUsername = r.getInterestedUser() != null ? r.getInterestedUser().getUsername() : null;
		String ownerUsername = (r.getListing() != null && r.getListing().getOwner() != null)
				? r.getListing().getOwner().getUsername()
				: null;

		return new ConnectionRequestResponseDto(
				r.getId(),
				r.getListing() != null ? r.getListing().getId() : null,
				r.getInterestedUser() != null ? r.getInterestedUser().getUserId() : null,
				interestedUsername,
				ownerUsername,
				r.getStatus(),
				r.getMessage(),
				r.getCreatedAt()
		);
	}
}
