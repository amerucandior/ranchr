package com.ranchr.ranchr.vet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vet")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Veterinary Officer"
)
public class VetController {

    @Operation(
            summary = "Enlist as a veterinary officer"
    )
    @PostMapping("/enlist")
    public ResponseEntity<?> enlistVeterinaryOfficer() {
        return null;
    }
}