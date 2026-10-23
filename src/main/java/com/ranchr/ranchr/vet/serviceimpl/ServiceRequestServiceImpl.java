package com.ranchr.ranchr.vet.serviceimpl;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.exceptions.BadRequestException;
import com.ranchr.exceptions.ConflictException;
import com.ranchr.exceptions.ForbiddenException;
import com.ranchr.exceptions.NotFoundException;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.vet.dto.*;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.VetRating;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import com.ranchr.ranchr.vet.repository.ServiceRequestRepository;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import com.ranchr.ranchr.vet.repository.VetRatingRepository;
import com.ranchr.ranchr.vet.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final VetProfileRepository vetProfileRepository;
    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final VetRatingRepository vetRatingRepository;
    private final SmsService smsService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ServiceRequestResponseDto createRequest(CreateServiceRequestDto dto, UUID farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new NotFoundException("Farmer user not found: " + farmerId));

        VetProfile targetVet = null;
        if (dto.targetVetId() != null) {
            targetVet = vetProfileRepository.findByVetId(dto.targetVetId())
                    .orElseThrow(() -> new NotFoundException("Target vet not found: " + dto.targetVetId()));

            if (targetVet.getStatus() != VerificationStatus.APPROVED) {
                throw new BadRequestException("Target veterinary officer is not yet verified by KVB.");
            }
        }

        ServiceRequest request = new ServiceRequest();
        request.setFarmer(farmer);
        request.setVetProfile(targetVet);
        request.setAnimalSpecies(dto.animalSpecies());
        request.setSymptoms(dto.symptoms());
        request.setDescription(dto.description());
        request.setLocation(dto.location());
        request.setAnimalPhotos(dto.animalPhotos());
        request.setStatus(ServiceStatus.PENDING);

        ServiceRequest saved = serviceRequestRepository.save(request);

        // 1. Notify Target Vet or Broadcast
        if (targetVet != null) {
            final VetProfile fVet = targetVet;
            findPhoneNumber(targetVet.getVet()).ifPresent(vetPhone -> {
                String alertPrefix = dto.isHeatAlert() ? "🚨 URGENT HEAT ALERT: " : "Ranchr Alert: ";
                String msg = String.format(
                        "%s%s requested your vet service for %s in %s. Symptoms: %s. Log in to accept.",
                        alertPrefix,
                        farmer.getUsername(),
                        dto.animalSpecies(),
                        dto.location(),
                        dto.symptoms()
                );
                sendSmsSafely(vetPhone, msg);
            });

            pushWebSocketNotification(targetVet.getVetId().toString(), Map.of(
                    "type", "NEW_SERVICE_REQUEST",
                    "serviceId", saved.getServiceId().toString(),
                    "animalSpecies", dto.animalSpecies(),
                    "isHeatAlert", dto.isHeatAlert()
            ));
        } else {
            // General broadcast to active vets
            try {
                if (messagingTemplate != null) {
                    messagingTemplate.convertAndSend("/topic/vets/broadcasts", (Object) Map.of(
                            "type", "BROADCAST_SERVICE_REQUEST",
                            "serviceId", saved.getServiceId().toString(),
                            "location", dto.location(),
                            "animalSpecies", dto.animalSpecies(),
                            "isHeatAlert", dto.isHeatAlert()
                    ));
                }
            } catch (Exception ex) {
                log.warn("[WebSocket] Broadcast failed: {}", ex.getMessage());
            }
        }

        // 2. Notify Farmer that request was submitted
        findPhoneNumber(farmer).ifPresent(farmerPhone -> {
            String msg = String.format(
                    "Ranchr Alert: Your service request for %s in %s is active. We will notify you once a vet confirms.",
                    dto.animalSpecies(),
                    dto.location()
            );
            sendSmsSafely(farmerPhone, msg);
        });

        return buildResponse(saved);
    }

    @Override
    @Transactional
    public ServiceRequestResponseDto acceptRequest(UUID serviceId, UUID vetId) {
        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseThrow(() -> new ForbiddenException("Only registered veterinary officers can accept requests"));

        if (vetProfile.getStatus() != VerificationStatus.APPROVED) {
            throw new ForbiddenException("Your veterinary profile is not yet approved by administrators.");
        }

        ServiceRequest request = serviceRequestRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service request not found: " + serviceId));

        if (request.getStatus() != ServiceStatus.PENDING) {
            throw new ConflictException("Service request is no longer pending (current status: " + request.getStatus() + ")");
        }

        if (request.getVetProfile() != null && !request.getVetProfile().getVetId().equals(vetId)) {
            throw new ForbiddenException("This request was assigned to another veterinary officer.");
        }

        request.setVetProfile(vetProfile);
        request.setStatus(ServiceStatus.ACCEPTED);
        request.setAcceptedAt(Instant.now());

        // Update vet availability to busy during active engagement
        vetProfile.setAvailable(Available.BUSY);
        vetProfileRepository.save(vetProfile);

        ServiceRequest saved = serviceRequestRepository.save(request);

        // Notify farmer via Africa's Talking SMS and WebSocket
        User farmer = request.getFarmer();
        Optional<String> vetPhone = findPhoneNumber(vetProfile.getVet());
        String vetContact = vetPhone.orElse(vetProfile.getVet().getEmail());

        findPhoneNumber(farmer).ifPresent(farmerPhone -> {
            String msg = String.format(
                    "Ranchr Alert: Dr. %s has ACCEPTED your service request for %s! Contact: %s to coordinate arrival.",
                    vetProfile.getKvbRegisteredName(),
                    request.getAnimalSpecies(),
                    vetContact
            );
            sendSmsSafely(farmerPhone, msg);
        });

        pushWebSocketNotification(farmer.getUserId().toString(), Map.of(
                "type", "SERVICE_ACCEPTED",
                "serviceId", saved.getServiceId().toString(),
                "vetName", vetProfile.getKvbRegisteredName(),
                "vetPhone", vetContact
        ));

        return buildResponse(saved);
    }

    @Override
    @Transactional
    public ServiceRequestResponseDto startService(UUID serviceId, UUID vetId) {
        ServiceRequest request = getOwnedVetRequestOrThrow(serviceId, vetId);

        if (request.getStatus() != ServiceStatus.ACCEPTED) {
            throw new ConflictException("Service must be ACCEPTED before starting (current: " + request.getStatus() + ")");
        }

        request.setStatus(ServiceStatus.IN_PROGRESS);
        ServiceRequest saved = serviceRequestRepository.save(request);

        User farmer = request.getFarmer();
        findPhoneNumber(farmer).ifPresent(farmerPhone -> {
            String msg = String.format(
                    "Ranchr Alert: Dr. %s has commenced service for your %s.",
                    request.getVetProfile().getKvbRegisteredName(),
                    request.getAnimalSpecies()
            );
            sendSmsSafely(farmerPhone, msg);
        });

        return buildResponse(saved);
    }

    @Override
    @Transactional
    public ServiceRequestResponseDto completeService(UUID serviceId, CompleteServiceRequestDto dto, UUID vetId) {
        ServiceRequest request = getOwnedVetRequestOrThrow(serviceId, vetId);

        if (request.getStatus() != ServiceStatus.ACCEPTED && request.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new ConflictException("Cannot complete service with status: " + request.getStatus());
        }

        request.setStatus(ServiceStatus.COMPLETED);
        request.setCompletedAt(Instant.now());

        if (dto != null) {
            StringBuilder notes = new StringBuilder();
            if (dto.semenStrawBatchNumber() != null && !dto.semenStrawBatchNumber().isBlank()) {
                notes.append(" [Semen Straw Batch: ").append(dto.semenStrawBatchNumber()).append("]");
            }
            if (dto.diagnosisNotes() != null && !dto.diagnosisNotes().isBlank()) {
                notes.append(" [Diagnosis: ").append(dto.diagnosisNotes()).append("]");
            }
            if (dto.prescribedMedication() != null && !dto.prescribedMedication().isBlank()) {
                notes.append(" [Medication: ").append(dto.prescribedMedication()).append("]");
            }
            if (dto.followUpInstructions() != null && !dto.followUpInstructions().isBlank()) {
                notes.append(" [Follow-up: ").append(dto.followUpInstructions()).append("]");
            }
            if (!notes.isEmpty()) {
                String existing = request.getDescription() != null ? request.getDescription() : "";
                request.setDescription(existing + notes);
            }
        }

        // Restore vet availability
        VetProfile vetProfile = request.getVetProfile();
        vetProfile.setAvailable(Available.AVAILABLE);
        vetProfileRepository.save(vetProfile);

        ServiceRequest saved = serviceRequestRepository.save(request);

        // Notify farmer to rate the vet
        User farmer = request.getFarmer();
        findPhoneNumber(farmer).ifPresent(farmerPhone -> {
            String msg = String.format(
                    "Ranchr Alert: Service for your %s is COMPLETED. Please rate your experience with Dr. %s on Ranchr.",
                    request.getAnimalSpecies(),
                    vetProfile.getKvbRegisteredName()
            );
            sendSmsSafely(farmerPhone, msg);
        });

        return buildResponse(saved);
    }

    @Override
    @Transactional
    public ServiceRequestResponseDto cancelRequest(UUID serviceId, UUID userId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service request not found: " + serviceId));

        boolean isFarmer = request.getFarmer().getUserId().equals(userId);
        boolean isVet = request.getVetProfile() != null && request.getVetProfile().getVetId().equals(userId);

        if (!isFarmer && !isVet) {
            throw new ForbiddenException("You are not authorized to cancel this service request.");
        }

        if (request.getStatus() == ServiceStatus.COMPLETED) {
            throw new ConflictException("Cannot cancel a service that is already COMPLETED.");
        }

        request.setStatus(ServiceStatus.CANCELLED);

        if (request.getVetProfile() != null) {
            request.getVetProfile().setAvailable(Available.AVAILABLE);
            vetProfileRepository.save(request.getVetProfile());
        }

        ServiceRequest saved = serviceRequestRepository.save(request);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponseDto getServiceById(UUID serviceId, UUID currentUserId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service request not found: " + serviceId));
        return buildResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponseDto> listFarmerRequests(UUID farmerId, Pageable pageable) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + farmerId));
        return serviceRequestRepository.findByFarmer(farmer, pageable).map(this::buildResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponseDto> listVetRequests(UUID vetId, ServiceStatus status, Pageable pageable) {
        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("Vet profile not found: " + vetId));

        Page<ServiceRequest> requests = (status != null)
                ? serviceRequestRepository.findByVetProfileAndStatus(vetProfile, status, pageable)
                : serviceRequestRepository.findByVetProfile(vetProfile, pageable);

        return requests.map(this::buildResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponseDto> listAvailableBroadcasts(String location, Pageable pageable) {
        Page<ServiceRequest> requests = (location != null && !location.isBlank())
                ? serviceRequestRepository.findByVetProfileIsNullAndStatusAndLocationContainingIgnoreCase(
                        ServiceStatus.PENDING, location, pageable)
                : serviceRequestRepository.findByVetProfileIsNullAndStatus(ServiceStatus.PENDING, pageable);

        return requests.map(this::buildResponse);
    }

    @Override
    @Transactional
    public VetRatingResponseDto rateService(UUID serviceId, CreateRatingDto ratingDto, UUID farmerId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service request not found: " + serviceId));

        if (!request.getFarmer().getUserId().equals(farmerId)) {
            throw new ForbiddenException("Only the farmer who requested this service can submit a rating.");
        }

        if (request.getStatus() != ServiceStatus.COMPLETED) {
            throw new ConflictException("You can only rate a service after it has been COMPLETED.");
        }

        if (vetRatingRepository.existsByService_ServiceId(serviceId)) {
            throw new ConflictException("You have already submitted a review for this service.");
        }

        if (request.getVetProfile() == null) {
            throw new BadRequestException("No veterinary officer was associated with this service.");
        }

        VetRating rating = new VetRating();
        rating.setVetProfile(request.getVetProfile());
        rating.setRatedBy(request.getFarmer());
        rating.setService(request);
        rating.setRating(ratingDto.rating());
        rating.setComment(ratingDto.comment());

        VetRating saved = vetRatingRepository.save(rating);
        return VetRatingResponseDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VetRatingResponseDto> listVetRatings(UUID vetId, Pageable pageable) {
        VetProfile vetProfile = vetProfileRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("Vet profile not found: " + vetId));
        return vetRatingRepository.findByVetProfile(vetProfile, pageable).map(VetRatingResponseDto::from);
    }

    private ServiceRequest getOwnedVetRequestOrThrow(UUID serviceId, UUID vetId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service request not found: " + serviceId));

        if (request.getVetProfile() == null || !request.getVetProfile().getVetId().equals(vetId)) {
            throw new ForbiddenException("You are not the veterinary officer assigned to this service.");
        }
        return request;
    }

    private ServiceRequestResponseDto buildResponse(ServiceRequest req) {
        String farmerPhone = findPhoneNumber(req.getFarmer()).orElse(null);
        String vetPhone = (req.getVetProfile() != null)
                ? findPhoneNumber(req.getVetProfile().getVet()).orElse(null)
                : null;

        return ServiceRequestResponseDto.from(req, farmerPhone, vetPhone);
    }

    private Optional<String> findPhoneNumber(User user) {
        if (user == null) return Optional.empty();
        return kycRepository.findByUser(user)
                .map(Kyc::getPhoneNumber)
                .filter(p -> p != null && !p.isBlank());
    }

    private void sendSmsSafely(String phoneNumber, String message) {
        try {
            smsService.send(phoneNumber, message);
            log.info("[SMS] Dispatched service alert to {}", phoneNumber);
        } catch (Exception ex) {
            log.warn("[SMS] Failed to send service alert to {}: {}", phoneNumber, ex.getMessage());
        }
    }

    private void pushWebSocketNotification(String userId, Object payload) {
        try {
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, (Object) payload);
            }
        } catch (Exception ex) {
            log.warn("[WebSocket] Failed to publish service notification to user {}: {}", userId, ex.getMessage());
        }
    }
}
