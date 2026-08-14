package com.ranchr.ranchr.vet.serviceimpl;

import com.ranchr.cloudinary.service.CloudinaryService;
import com.ranchr.ranchr.vet.dto.VerficationRequest;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import com.ranchr.ranchr.vet.service.VetProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class VetProfileServiceImpl implements VetProfileService {

    private final VetProfileRepository vetProfileRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public void verifyVetProfile(VerficationRequest request, UUID vetId) {

    }

    @Override
    @Transactional
    public void toggleAvailability(UUID vetId) {

    }

    @Override
    @Transactional
    public void updateVetProfile(List<MultipartFile> files, UUID vetId) {

    }

    @Override
    @Transactional(readOnly = true)
    public void listVetProfiles() {

    }
}
