package com.ranchr.ranchr.vet.serviceimpl;

import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.cloudinary.service.CloudinaryService;
import com.ranchr.exceptions.NotFoundException;
import com.ranchr.ranchr.vet.dto.CreateVetProfileRequest;
import com.ranchr.ranchr.vet.dto.PresenceEvent;
import com.ranchr.ranchr.vet.dto.VerficationRequest;
import com.ranchr.ranchr.vet.dto.VetProfileResponseDto;
import com.ranchr.ranchr.vet.dto.enums.PresenceEventType;
import com.ranchr.ranchr.vet.dto.enums.Verify;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import com.ranchr.ranchr.vet.service.VetProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VetProfileServiceImpl implements VetProfileService {

    private final VetProfileRepository vetProfileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public VetProfileResponseDto enlistVetProfile(CreateVetProfileRequest request, UUID vetId) {
        User user = userRepository.findById(vetId)
                .orElseThrow(() -> new NotFoundException("User not found: " + vetId));

        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseGet(() -> VetProfile.builder()
                        .vet(user)
                        .kvbRegisteredName(user.getUsername())
                        .kvbLicenseNumber("PENDING_VERIFICATION")
                        .kvbIdentificationCardFrontUrl("PENDING_UPLOAD")
                        .kvbIdentificationCardBackUrl("PENDING_UPLOAD")
                        .yearsExperience(request.experienceYears() != null ? request.experienceYears() : 0)
                        .servicesOffered(request.servicesOffered())
                        .status(VerificationStatus.UNDER_REVIEW)
                        .verified(VerificationStatus.UNDER_REVIEW)
                        .available(Available.AVAILABLE)
                        .build());

        if (request.experienceYears() != null) {
            vetProfile.setYearsExperience(request.experienceYears());
        }
        if (request.servicesOffered() != null && !request.servicesOffered().isEmpty()) {
            vetProfile.setServicesOffered(request.servicesOffered());
        }

        user.setRole(Role.VET);
        userRepository.save(user);

        VetProfile saved = vetProfileRepository.save(vetProfile);
        return VetProfileResponseDto.from(saved);
    }

    @Override
    @Transactional
    public void verifyVetProfile(VerficationRequest request, UUID vetId) {
        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("Vet profile not found: " + vetId));

        VerificationStatus newStatus = (request.verify() == Verify.ACCEPT)
                ? VerificationStatus.APPROVED
                : VerificationStatus.REJECTED;

        vetProfile.setStatus(newStatus);
        vetProfile.setVerified(newStatus);
        vetProfile.setVerifiedAt(LocalDateTime.now());
        vetProfileRepository.save(vetProfile);
    }

    @Override
    @Transactional
    public void toggleAvailability(PresenceEvent event, UUID vetId) {
        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("Vet profile not found: " + vetId));

        // Fixed logic: GO_ONLINE makes vet AVAILABLE, GO_OFFLINE makes vet OFFLINE
        Available newAvailability = (event.getType() == PresenceEventType.GO_ONLINE)
                ? Available.AVAILABLE
                : Available.OFFLINE;

        vetProfile.setAvailable(newAvailability);
        vetProfileRepository.save(vetProfile);
    }

    @Override
    @Transactional
    public VetProfileResponseDto updateVetProfile(MultipartFile idCardFront,
                                                  MultipartFile idCardBack,
                                                  MultipartFile kvbLicense,
                                                  String kvbRegisteredName,
                                                  UUID vetId) {

        User user = userRepository.findById(vetId)
                .orElseThrow(() -> new NotFoundException("User not found: " + vetId));

        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseGet(() -> VetProfile.builder()
                        .vet(user)
                        .kvbRegisteredName(user.getUsername())
                        .kvbLicenseNumber("PENDING_VERIFICATION")
                        .kvbIdentificationCardFrontUrl("PENDING_UPLOAD")
                        .kvbIdentificationCardBackUrl("PENDING_UPLOAD")
                        .yearsExperience(0)
                        .status(VerificationStatus.UNDER_REVIEW)
                        .verified(VerificationStatus.UNDER_REVIEW)
                        .available(Available.AVAILABLE)
                        .build());

        String folder = "vets/" + vetId + "/kvb-id";

        if (idCardFront != null && !idCardFront.isEmpty()) {
            vetProfile.setKvbIdentificationCardFrontUrl(uploadSingleFile(idCardFront, folder, "_front"));
        }

        if (idCardBack != null && !idCardBack.isEmpty()) {
            vetProfile.setKvbIdentificationCardBackUrl(uploadSingleFile(idCardBack, folder, "_back"));
        }

        if (kvbLicense != null && !kvbLicense.isEmpty()) {
            vetProfile.setKvbLicenseNumber(uploadSingleFile(kvbLicense, folder, "_license"));
        }

        if (kvbRegisteredName != null && !kvbRegisteredName.isBlank()) {
            vetProfile.setKvbRegisteredName(kvbRegisteredName);
        }

        user.setRole(Role.VET);
        userRepository.save(user);

        VetProfile saved = vetProfileRepository.save(vetProfile);
        return VetProfileResponseDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VetProfileResponseDto> listVetProfiles(Available available, VerificationStatus status) {
        if (available != null && status != null) {
            return vetProfileRepository.findAllByAvailableAndVerified(available, status).stream()
                    .map(VetProfileResponseDto::from)
                    .toList();
        } else if (status != null) {
            return vetProfileRepository.findAllByVerified(status).stream()
                    .map(VetProfileResponseDto::from)
                    .toList();
        } else {
            return vetProfileRepository.findAll().stream()
                    .map(VetProfileResponseDto::from)
                    .toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VetProfileResponseDto getVetProfile(UUID vetId) {
        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("Vet profile not found: " + vetId));
        return VetProfileResponseDto.from(vetProfile);
    }

    private String uploadSingleFile(MultipartFile file, String folder, String publicId) {
        Map<String, Object> result = cloudinaryService.uploadFile(file, folder, publicId);
        return (String) result.get("secure_url");
    }
}
