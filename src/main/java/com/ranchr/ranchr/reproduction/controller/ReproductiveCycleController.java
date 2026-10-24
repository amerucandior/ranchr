package com.ranchr.ranchr.reproduction.controller;

import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.ranchr.reproduction.dto.CreateReproductiveCycleDto;
import com.ranchr.ranchr.reproduction.dto.ReproductiveCycleResponseDto;
import com.ranchr.ranchr.reproduction.service.ReproductiveCycleService;
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
@RequestMapping("/api/reproduction")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Reproductive Lifecycle Management",
        description = "Day 21 non-return check, Day 60 pregnancy diagnosis, Day 280 calving tracking, and automated reminders"
)
public class ReproductiveCycleController {

    private final ReproductiveCycleService reproductiveCycleService;
    private final CurrentUser currentUser;

    @PostMapping("/cycles")
    @Operation(summary = "Register a new reproductive tracking cycle")
    public ResponseEntity<ReproductiveCycleResponseDto> create(
            @Valid @RequestBody CreateReproductiveCycleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reproductiveCycleService.createCycle(dto, currentUser.getUserId()));
    }

    @GetMapping("/cycles")
    @Operation(summary = "List reproductive cycles for current farmer with countdown milestones")
    public ResponseEntity<Page<ReproductiveCycleResponseDto>> listFarmerCycles(Pageable pageable) {
        return ResponseEntity.ok(reproductiveCycleService.listFarmerCycles(currentUser.getUserId(), pageable));
    }

    @GetMapping("/cycles/{id}")
    @Operation(summary = "Get reproductive cycle details and milestone dates by ID")
    public ResponseEntity<ReproductiveCycleResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reproductiveCycleService.getCycleById(id, currentUser.getUserId()));
    }

    @PutMapping("/cycles/{id}/confirm-pregnancy")
    @Operation(summary = "Confirm gestation following veterinary pregnancy diagnosis")
    public ResponseEntity<ReproductiveCycleResponseDto> confirmPregnancy(@PathVariable UUID id) {
        return ResponseEntity.ok(reproductiveCycleService.confirmPregnancy(id, currentUser.getUserId()));
    }

    @PutMapping("/cycles/{id}/report-heat")
    @Operation(summary = "Report that animal returned to heat around Day 21 (failed conception)")
    public ResponseEntity<ReproductiveCycleResponseDto> reportHeatReturned(@PathVariable UUID id) {
        return ResponseEntity.ok(reproductiveCycleService.reportHeatReturned(id, currentUser.getUserId()));
    }

    @PutMapping("/cycles/{id}/calve")
    @Operation(summary = "Report successful delivery/calving")
    public ResponseEntity<ReproductiveCycleResponseDto> reportCalved(@PathVariable UUID id) {
        return ResponseEntity.ok(reproductiveCycleService.reportCalved(id, currentUser.getUserId()));
    }

    @PostMapping("/admin/trigger-scheduler")
    @Operation(summary = "Manually execute the daily reproductive alert evaluation")
    public ResponseEntity<String> triggerScheduler() {
        reproductiveCycleService.processScheduledAlerts();
        return ResponseEntity.ok("Reproductive milestone scheduler processed successfully.");
    }
}
