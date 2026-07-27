package com.ranchr.ranchr.listing.controller;

import com.ranchr.ranchr.listing.dto.CreateListingRequest;
import com.ranchr.ranchr.listing.dto.ListingResponse;
import com.ranchr.ranchr.listing.dto.UpdateListingRequest;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import com.ranchr.ranchr.listing.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@Tag(
		name = "Listings",
		description = "Animal service listings — " +
							  "create, search, update, remove, and record interest"
)
public class ListingController {

	private final ListingService listingService;

	public ListingController(ListingService listingService) {
		this.listingService = listingService;
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "Create a listing",
			description = "Creates a new animal service listing with optional image uploads (max 10 files). " +
								  "Send as multipart/form-data with a 'listing' JSON part and optional 'files' part."
	)
	@ApiResponse(responseCode = "201", description = "Listing created")
	@ApiResponse(responseCode = "502", description = "Image upload to Cloudinary failed")
	public ResponseEntity<ListingResponse> create(
			@RequestPart("listing") @Valid CreateListingRequest request,
			@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		ListingResponse created = listingService.create(request, files);
		return ResponseEntity.created(URI.create("/api/listings/" + created.id())).body(created);
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Get a listing by ID",
			description = "Returns full listing details including images. Public — no auth required."
	)
	@ApiResponse(responseCode = "200", description = "Listing found")
	@ApiResponse(responseCode = "404", description = "Listing not found")
	public ListingResponse getById(@PathVariable UUID id) {
		return listingService.getById(id);
	}

	@GetMapping
	@Operation(
			summary = "Search listings",
			description = "Searches ACTIVE listings by default. Pass ?status= explicitly to see INACTIVE/EXPIRED/REMOVED. " +
								  "Anonymous users see at most 12 results sorted newest-first. Authenticated users get full pagination."
	)
	public Page<ListingResponse> search(@RequestParam(required = false) String animalType,
	                                    @RequestParam(required = false) String location,
	                                    @RequestParam(required = false) ListingServiceType serviceType,
	                                    @RequestParam(required = false) ListingStatus status,
	                                    Pageable pageable) {
		return listingService.search(animalType, location, serviceType, status, pageable);
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Update a listing",
			description = "Full-replace update. Must be the listing owner. Omit status to leave current status unchanged."
	)
	@ApiResponse(responseCode = "200", description = "Listing updated")
	@ApiResponse(responseCode = "403", description = "Not the listing owner")
	@ApiResponse(responseCode = "404", description = "Listing not found")
	@SecurityRequirement(name = "bearerAuth")
	public ListingResponse update(@PathVariable UUID id,
	                              @Valid @RequestBody UpdateListingRequest request) {
		return listingService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "Remove a listing",
			description = "Soft-delete: sets status to REMOVED. Must be the listing owner."
	)
	@ApiResponse(responseCode = "204", description = "Listing removed (no body)")
	@ApiResponse(responseCode = "403", description = "Not the listing owner")
	@ApiResponse(responseCode = "404", description = "Listing not found")
	public void remove(@PathVariable UUID id) {
		listingService.remove(id);
	}

	@PostMapping("/{id}/interest")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			summary = "Record interest in a listing",
			description = "Increments the listing's interest counter. No request body."
	)
	@ApiResponse(responseCode = "204", description = "Interest recorded (no body)")
	@ApiResponse(responseCode = "404", description = "Listing not found")
	public void recordInterest(@PathVariable UUID id) {
		listingService.recordInterest(id);
	}
}