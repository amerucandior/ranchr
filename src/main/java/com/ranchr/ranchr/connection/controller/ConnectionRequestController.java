package com.ranchr.ranchr.connection.controller;

import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.exceptions.NotFoundException;
import com.ranchr.ranchr.connection.dto.ConnectionRequestCreateDto;
import com.ranchr.ranchr.connection.dto.ConnectionRequestResponseDto;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.connection.service.ConnectionRequestService;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.repository.ListingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
		name = "Connections",
		description = "Connection requests between listing owners and interested users — send, accept, reject, and list")
public class ConnectionRequestController {

	private final ConnectionRequestService connectionRequestService;
	private final ListingRepository listingRepository;
	private final ConnectionRequestRepository connectionRequestRepository;
	private final CurrentUser currentUser;

	@PostMapping
	@Operation(
			summary = "Send a connection request",
			description = "Message is optional (max 1000 chars). Cannot request your own listing. " +
								  "Listing must be ACTIVE. No duplicate PENDING or ACCEPTED requests allowed. " +
								  "Previously rejected requests are blocked until the owner reactivates."
	)
	@ApiResponse(responseCode = "200", description = "Request created")
	@ApiResponse(responseCode = "400", description = "Your own listing or listing not active")
	@ApiResponse(responseCode = "404", description = "Listing not found")
	@ApiResponse(responseCode = "409", description = "Duplicate request or blocked by reactivation cycle")
	public ConnectionRequestResponseDto create(
			@RequestBody @Valid ConnectionRequestCreateDto dto) {

		Listing listing = listingRepository.findById(dto.listingId())
								  .orElseThrow(() -> new NotFoundException("Listing not found."));

		ConnectionRequest request = connectionRequestService.create(
				listing, currentUser.get(), dto.message());
		return ConnectionRequestResponseDto.from(request);
	}

	@GetMapping("/incoming")
	@Operation(
			summary = "List incoming connection requests",
			description = "Returns requests for listings you own. Paginated."
	)
	public Page<ConnectionRequestResponseDto> incoming(Pageable pageable) {
		return connectionRequestRepository.findByListing_Owner(currentUser.get(), pageable)
					   .map(ConnectionRequestResponseDto::from);
	}

	@GetMapping("/outgoing")
	@Operation(
			summary = "List outgoing connection requests",
			description = "Returns requests you sent. Paginated."
	)
	public Page<ConnectionRequestResponseDto> outgoing(Pageable pageable) {
		return connectionRequestRepository.findByInterestedUser(currentUser.get(), pageable)
					   .map(ConnectionRequestResponseDto::from);
	}

	@PutMapping("/{id}/accept")
	@Operation(
			summary = "Accept a connection request",
			description = "Must own the listing the request targets. Only PENDING requests can be accepted."
	)
	@ApiResponse(responseCode = "200", description = "Request accepted")
	@ApiResponse(responseCode = "403", description = "Not the listing owner")
	@ApiResponse(responseCode = "404", description = "Request not found")
	@ApiResponse(responseCode = "409", description = "Request is not PENDING")
	public ConnectionRequestResponseDto accept(
			@PathVariable UUID id) {
		return ConnectionRequestResponseDto.from(
				connectionRequestService.accept(id, currentUser.get()));
	}

	@PutMapping("/{id}/reject")
	@Operation(
			summary = "Reject a connection request",
			description = "Must own the listing the request targets. Only PENDING requests can be rejected. " +
								  "Decrements the listing's interestCount."
	)
	@ApiResponse(responseCode = "200", description = "Request rejected")
	@ApiResponse(responseCode = "403", description = "Not the listing owner")
	@ApiResponse(responseCode = "404", description = "Request not found")
	@ApiResponse(responseCode = "409", description = "Request is not PENDING")
	public ConnectionRequestResponseDto reject(
			@PathVariable UUID id) {
		return ConnectionRequestResponseDto.from(
				connectionRequestService.reject(id, currentUser.get()));
	}
}