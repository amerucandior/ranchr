package com.ranchr.ranchr.vet.service;

import com.ranchr.ranchr.vet.dto.*;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServiceRequestService {
    ServiceRequestResponseDto createRequest(CreateServiceRequestDto dto, UUID farmerId);
    ServiceRequestResponseDto acceptRequest(UUID serviceId, UUID vetId);
    ServiceRequestResponseDto startService(UUID serviceId, UUID vetId);
    ServiceRequestResponseDto completeService(UUID serviceId, CompleteServiceRequestDto dto, UUID vetId);
    ServiceRequestResponseDto cancelRequest(UUID serviceId, UUID userId);
    ServiceRequestResponseDto getServiceById(UUID serviceId, UUID currentUserId);
    Page<ServiceRequestResponseDto> listFarmerRequests(UUID farmerId, Pageable pageable);
    Page<ServiceRequestResponseDto> listVetRequests(UUID vetId, ServiceStatus status, Pageable pageable);
    Page<ServiceRequestResponseDto> listAvailableBroadcasts(String location, Pageable pageable);
    VetRatingResponseDto rateService(UUID serviceId, CreateRatingDto ratingDto, UUID farmerId);
    Page<VetRatingResponseDto> listVetRatings(UUID vetId, Pageable pageable);
}
