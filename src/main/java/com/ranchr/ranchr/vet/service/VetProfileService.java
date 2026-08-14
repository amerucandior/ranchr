package com.ranchr.ranchr.vet.service;

import com.ranchr.ranchr.vet.dto.VerficationRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface VetProfileService {
    void verifyVetProfile(VerficationRequest request, UUID vetId);
    void toggleAvailability(UUID vetId);
    void updateVetProfile(List<MultipartFile> files, UUID vetId);
    void listVetProfiles();
}
