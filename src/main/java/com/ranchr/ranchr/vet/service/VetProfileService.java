package com.ranchr.ranchr.vet.service;

import com.ranchr.ranchr.vet.dto.CreateVetProfileRequest;
import com.ranchr.ranchr.vet.dto.PresenceEvent;
import com.ranchr.ranchr.vet.dto.VerficationRequest;
import com.ranchr.ranchr.vet.dto.VetProfileResponseDto;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface VetProfileService {
    VetProfileResponseDto enlistVetProfile(CreateVetProfileRequest request, UUID vetId);
    void verifyVetProfile(VerficationRequest request, UUID vetId);
    void toggleAvailability(PresenceEvent event, UUID vetId);
    VetProfileResponseDto updateVetProfile(
            MultipartFile idCardFront, MultipartFile idCardBack, MultipartFile kvbLicense, String kvbRegisteredName, UUID vetId);
    List<VetProfileResponseDto> listVetProfiles(Available available, VerificationStatus status);
    VetProfileResponseDto getVetProfile(UUID vetId);
}
