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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionRequestController {

	private final ConnectionRequestService connectionRequestService;
	private final ListingRepository listingRepository;
	private final ConnectionRequestRepository connectionRequestRepository;
	private final CurrentUser currentUser;

	@PostMapping
	public ConnectionRequestResponseDto create(
			@RequestBody @Valid ConnectionRequestCreateDto dto) {

		Listing listing = listingRepository.findById(dto.listingId())
								  .orElseThrow(() -> new NotFoundException("Listing not found."));

		ConnectionRequest request = connectionRequestService.create(
				listing, currentUser.get(), dto.message());
		return ConnectionRequestResponseDto.from(request);
	}

	@GetMapping("/incoming")
	public Page<ConnectionRequestResponseDto> incoming(Pageable pageable) {
		return connectionRequestRepository.findByListing_Owner(currentUser.get(), pageable)
					   .map(ConnectionRequestResponseDto::from);
	}

	@GetMapping("/outgoing")
	public Page<ConnectionRequestResponseDto> outgoing(Pageable pageable) {
		return connectionRequestRepository.findByInterestedUser(currentUser.get(), pageable)
					   .map(ConnectionRequestResponseDto::from);
	}

	@PutMapping("/{id}/accept")
	public ConnectionRequestResponseDto accept(
			@PathVariable UUID id) {
		return ConnectionRequestResponseDto.from(
				connectionRequestService.accept(id, currentUser.get()));
	}

	@PutMapping("/{id}/reject")
	public ConnectionRequestResponseDto reject(
			@PathVariable UUID id) {
		return ConnectionRequestResponseDto.from(
				connectionRequestService.reject(id, currentUser.get()));
	}
}