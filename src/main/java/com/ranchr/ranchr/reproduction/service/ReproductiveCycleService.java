package com.ranchr.ranchr.reproduction.service;

import com.ranchr.ranchr.reproduction.dto.CreateReproductiveCycleDto;
import com.ranchr.ranchr.reproduction.dto.ReproductiveCycleResponseDto;
import com.ranchr.ranchr.reproduction.model.ReproductiveCycle;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReproductiveCycleService {
    ReproductiveCycleResponseDto createCycle(CreateReproductiveCycleDto dto, UUID farmerId);
    ReproductiveCycle createCycleFromServiceRequest(ServiceRequest request, String semenStrawBatch);
    ReproductiveCycleResponseDto getCycleById(UUID id, UUID currentUserId);
    Page<ReproductiveCycleResponseDto> listFarmerCycles(UUID farmerId, Pageable pageable);
    ReproductiveCycleResponseDto confirmPregnancy(UUID id, UUID userId);
    ReproductiveCycleResponseDto reportHeatReturned(UUID id, UUID userId);
    ReproductiveCycleResponseDto reportCalved(UUID id, UUID userId);
    void processScheduledAlerts();
}
