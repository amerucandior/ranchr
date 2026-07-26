package com.ranchr.ranchr.listing.controller;

import com.ranchr.ranchr.listing.dto.CreateListingRequest;
import com.ranchr.ranchr.listing.dto.ListingResponse;
import com.ranchr.ranchr.listing.dto.UpdateListingRequest;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import com.ranchr.ranchr.listing.service.ListingService;
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
public class ListingController {

	private final ListingService listingService;

	public ListingController(ListingService listingService) {
		this.listingService = listingService;
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ListingResponse> create(
			@RequestPart("listing") @Valid CreateListingRequest request,
			@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		ListingResponse created = listingService.create(request, files);
		return ResponseEntity.created(URI.create("/api/listings/" + created.id())).body(created);
	}

	@GetMapping("/{id}")
	public ListingResponse getById(@PathVariable UUID id) {
		return listingService.getById(id);
	}

	@GetMapping
	public Page<ListingResponse> search(@RequestParam(required = false) String animalType,
	                                    @RequestParam(required = false) String location,
	                                    @RequestParam(required = false) ListingServiceType serviceType,
	                                    @RequestParam(required = false) ListingStatus status,
	                                    Pageable pageable) {
		return listingService.search(animalType, location, serviceType, status, pageable);
	}

	@PutMapping("/{id}")
	public ListingResponse update(@PathVariable UUID id,
	                              @Valid @RequestBody UpdateListingRequest request) {
		return listingService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable UUID id) {
		listingService.remove(id);
	}

	@PostMapping("/{id}/interest")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void recordInterest(@PathVariable UUID id) {
		listingService.recordInterest(id);
	}
}