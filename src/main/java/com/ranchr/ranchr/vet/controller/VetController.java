package com.ranchr.ranchr.vet.controller;

import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.ranchr.vet.dto.CreateVetProfileRequest;
import com.ranchr.ranchr.vet.dto.PresenceEvent;
import com.ranchr.ranchr.vet.dto.VerficationRequest;
import com.ranchr.ranchr.vet.dto.VetProfileResponseDto;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import com.ranchr.ranchr.vet.service.VetProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vet")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Veterinary Officer"
)
public class VetController {

    private final VetProfileService vetProfileService;
    private final CurrentUser currentUser;

    @Operation(
            summary = "Enlist as a veterinary officer"
    )
    @PostMapping("/enlist")
    public ResponseEntity<VetProfileResponseDto> enlist(
            @Valid @RequestBody CreateVetProfileRequest request) {
        VetProfileResponseDto response = vetProfileService.enlistVetProfile(request, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Update veterinary officer details and KVB documents"
    )
    @PutMapping(value = "/vets/update-kvb-verification",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<VetProfileResponseDto> updateKvbVerification(
            @RequestParam(required = false) String kvbRegisteredName,
            @RequestParam(required = false) MultipartFile kvbLicenseNumber,
            @RequestParam(value = "idCardFront", required = false) MultipartFile idCardFront,
            @RequestParam(value = "idCardBack", required = false) MultipartFile idCardBack
    ) {
        VetProfileResponseDto updated = vetProfileService.updateVetProfile(
                idCardFront, idCardBack, kvbLicenseNumber, kvbRegisteredName, currentUser.getUserId());
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "List veterinary officers with optional filters"
    )
    @GetMapping("/vets")
    public ResponseEntity<List<VetProfileResponseDto>> listVets(
            @RequestParam(required = false) Available available,
            @RequestParam(required = false) VerificationStatus status) {
        return ResponseEntity.ok(vetProfileService.listVetProfiles(available, status));
    }

    @Operation(
            summary = "Get a veterinary officer's profile by ID"
    )
    @GetMapping("/vets/{id}")
    public ResponseEntity<VetProfileResponseDto> getVetById(@PathVariable UUID id) {
        return ResponseEntity.ok(vetProfileService.getVetProfile(id));
    }

    @Operation(
            summary = "Verify or reject a veterinary officer's profile"
    )
    @PutMapping("/vets/{id}/verify")
    public ResponseEntity<Void> verifyVetProfile(
            @PathVariable UUID id,
            @Valid @RequestBody VerficationRequest request) {
        vetProfileService.verifyVetProfile(request, id);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("vets/availability")
    public void updateAvailability(@Payload PresenceEvent event) {
        vetProfileService.toggleAvailability(event, currentUser.getUserId());
    }
}
