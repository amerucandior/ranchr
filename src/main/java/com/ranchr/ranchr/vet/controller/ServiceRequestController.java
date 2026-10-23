package com.ranchr.ranchr.vet.controller;

import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.ranchr.vet.dto.*;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import com.ranchr.ranchr.vet.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Veterinary Service Dispatch",
        description = "Urgency-tiered booking, fast-path heat dispatch, veterinary acceptance, and rating loop"
)
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final CurrentUser currentUser;

    @PostMapping
    @Operation(summary = "Create a service request or broadcast heat alert")
    public ResponseEntity<ServiceRequestResponseDto> create(
            @Valid @RequestBody CreateServiceRequestDto dto) {
        ServiceRequestResponseDto response = serviceRequestService.createRequest(dto, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service request details by ID")
    public ResponseEntity<ServiceRequestResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceRequestService.getServiceById(id, currentUser.getUserId()));
    }

    @GetMapping("/farmer")
    @Operation(summary = "List service requests initiated by current farmer")
    public ResponseEntity<Page<ServiceRequestResponseDto>> listFarmerRequests(Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.listFarmerRequests(currentUser.getUserId(), pageable));
    }

    @GetMapping("/vet")
    @Operation(summary = "List service requests assigned to current veterinary officer")
    public ResponseEntity<Page<ServiceRequestResponseDto>> listVetRequests(
            @RequestParam(required = false) ServiceStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.listVetRequests(currentUser.getUserId(), status, pageable));
    }

    @GetMapping("/broadcasts")
    @Operation(summary = "List pending unassigned broadcast requests for vets")
    public ResponseEntity<Page<ServiceRequestResponseDto>> listBroadcasts(
            @RequestParam(required = false) String location,
            Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.listAvailableBroadcasts(location, pageable));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Veterinary officer accepts a service request")
    public ResponseEntity<ServiceRequestResponseDto> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceRequestService.acceptRequest(id, currentUser.getUserId()));
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "Veterinary officer marks service as IN_PROGRESS (en route/on-site)")
    public ResponseEntity<ServiceRequestResponseDto> start(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceRequestService.startService(id, currentUser.getUserId()));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Veterinary officer completes service and logs semen straw batch/notes")
    public ResponseEntity<ServiceRequestResponseDto> complete(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteServiceRequestDto dto) {
        return ResponseEntity.ok(serviceRequestService.completeService(id, dto, currentUser.getUserId()));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Farmer or assigned vet cancels a service request")
    public ResponseEntity<ServiceRequestResponseDto> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceRequestService.cancelRequest(id, currentUser.getUserId()));
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Farmer rates a completed service (1-5 stars + review)")
    public ResponseEntity<VetRatingResponseDto> rate(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRatingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceRequestService.rateService(id, dto, currentUser.getUserId()));
    }

    @GetMapping("/vets/{vetId}/ratings")
    @Operation(summary = "Get ratings and reviews for a veterinary officer")
    public ResponseEntity<Page<VetRatingResponseDto>> listVetRatings(
            @PathVariable UUID vetId,
            Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.listVetRatings(vetId, pageable));
    }
}
