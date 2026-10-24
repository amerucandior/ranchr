package com.ranchr.ranchr.reproduction.serviceimpl;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.exceptions.ForbiddenException;
import com.ranchr.exceptions.NotFoundException;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.repository.ListingRepository;
import com.ranchr.ranchr.reproduction.dto.CreateReproductiveCycleDto;
import com.ranchr.ranchr.reproduction.dto.ReproductiveCycleResponseDto;
import com.ranchr.ranchr.reproduction.model.ReproductiveCycle;
import com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus;
import com.ranchr.ranchr.reproduction.repository.ReproductiveCycleRepository;
import com.ranchr.ranchr.reproduction.service.ReproductiveCycleService;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.repository.ServiceRequestRepository;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReproductiveCycleServiceImpl implements ReproductiveCycleService {

    private final ReproductiveCycleRepository reproductiveCycleRepository;
    private final UserRepository userRepository;
    private final VetProfileRepository vetProfileRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ListingRepository listingRepository;
    private final KycRepository kycRepository;
    private final SmsService smsService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ReproductiveCycleResponseDto createCycle(CreateReproductiveCycleDto dto, UUID farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new NotFoundException("Farmer not found: " + farmerId));

        LocalDate insemDate = dto.inseminationDate() != null ? dto.inseminationDate() : LocalDate.now();

        ServiceRequest serviceReq = null;
        if (dto.serviceId() != null) {
            serviceReq = serviceRequestRepository.findById(dto.serviceId()).orElse(null);
        }

        Listing listing = null;
        if (dto.listingId() != null) {
            listing = listingRepository.findById(dto.listingId()).orElse(null);
        }

        VetProfile vet = null;
        if (dto.vetId() != null) {
            vet = vetProfileRepository.findByVetId(dto.vetId()).orElse(null);
        } else if (serviceReq != null && serviceReq.getVetProfile() != null) {
            vet = serviceReq.getVetProfile();
        }

        ReproductiveCycle cycle = ReproductiveCycle.builder()
                .farmer(farmer)
                .serviceRequest(serviceReq)
                .listing(listing)
                .vetProfile(vet)
                .animalIdentifier(dto.animalIdentifier())
                .animalSpecies(dto.animalSpecies())
                .semenStrawBatchNumber(dto.semenStrawBatchNumber())
                .inseminationDate(insemDate)
                .nonReturnCheckDate(calculateNonReturnDate(dto.animalSpecies(), insemDate))
                .pregnancyDiagnosisDate(calculatePregnancyDiagnosisDate(dto.animalSpecies(), insemDate))
                .expectedCalvingDate(calculateExpectedCalvingDate(dto.animalSpecies(), insemDate))
                .status(ReproductiveStatus.INSEMINATED)
                .notes(dto.notes())
                .build();

        ReproductiveCycle saved = reproductiveCycleRepository.save(cycle);
        log.info("[Reproduction] Registered reproductive cycle for '{}', Day 21 on {}",
                saved.getAnimalIdentifier(), saved.getNonReturnCheckDate());

        return ReproductiveCycleResponseDto.from(saved);
    }

    @Override
    @Transactional
    public ReproductiveCycle createCycleFromServiceRequest(ServiceRequest request, String semenStrawBatch) {
        // Prevent duplicate creation if already tracked
        Optional<ReproductiveCycle> existing = reproductiveCycleRepository.findByServiceRequest_ServiceId(request.getServiceId());
        if (existing.isPresent()) {
            return existing.get();
        }

        LocalDate insemDate = LocalDate.now();
        String species = request.getAnimalSpecies() != null ? request.getAnimalSpecies() : "Cattle";
        String identifier = species + " (Service #" + request.getServiceId().toString().substring(0, 8) + ")";

        ReproductiveCycle cycle = ReproductiveCycle.builder()
                .farmer(request.getFarmer())
                .serviceRequest(request)
                .vetProfile(request.getVetProfile())
                .animalIdentifier(identifier)
                .animalSpecies(species)
                .semenStrawBatchNumber(semenStrawBatch)
                .inseminationDate(insemDate)
                .nonReturnCheckDate(calculateNonReturnDate(species, insemDate))
                .pregnancyDiagnosisDate(calculatePregnancyDiagnosisDate(species, insemDate))
                .expectedCalvingDate(calculateExpectedCalvingDate(species, insemDate))
                .status(ReproductiveStatus.INSEMINATED)
                .notes("Automated tracking created from completed service request")
                .build();

        return reproductiveCycleRepository.save(cycle);
    }

    @Override
    @Transactional(readOnly = true)
    public ReproductiveCycleResponseDto getCycleById(UUID id, UUID currentUserId) {
        ReproductiveCycle cycle = reproductiveCycleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reproductive cycle not found: " + id));
        return ReproductiveCycleResponseDto.from(cycle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReproductiveCycleResponseDto> listFarmerCycles(UUID farmerId, Pageable pageable) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + farmerId));
        return reproductiveCycleRepository.findByFarmer(farmer, pageable)
                .map(ReproductiveCycleResponseDto::from);
    }

    @Override
    @Transactional
    public ReproductiveCycleResponseDto confirmPregnancy(UUID id, UUID userId) {
        ReproductiveCycle cycle = getOwnedCycleOrThrow(id, userId);
        cycle.setStatus(ReproductiveStatus.CONFIRMED_PREGNANT);
        ReproductiveCycle saved = reproductiveCycleRepository.save(cycle);

        findPhoneNumber(cycle.getFarmer()).ifPresent(phone -> {
            String msg = String.format(
                    "Ranchr Alert: Gestation CONFIRMED for %s! Expected calving date: %s. Maternity reminders will follow.",
                    cycle.getAnimalIdentifier(),
                    cycle.getExpectedCalvingDate()
            );
            sendSmsSafely(phone, msg);
        });

        return ReproductiveCycleResponseDto.from(saved);
    }

    @Override
    @Transactional
    public ReproductiveCycleResponseDto reportHeatReturned(UUID id, UUID userId) {
        ReproductiveCycle cycle = getOwnedCycleOrThrow(id, userId);
        cycle.setStatus(ReproductiveStatus.HEAT_RETURNED);
        ReproductiveCycle saved = reproductiveCycleRepository.save(cycle);

        findPhoneNumber(cycle.getFarmer()).ifPresent(phone -> {
            String msg = String.format(
                    "Ranchr Alert: Return to heat logged for %s. Optimal 12-hour breeding window is active. Request repeat AI now on Ranchr.",
                    cycle.getAnimalIdentifier()
            );
            sendSmsSafely(phone, msg);
        });

        return ReproductiveCycleResponseDto.from(saved);
    }

    @Override
    @Transactional
    public ReproductiveCycleResponseDto reportCalved(UUID id, UUID userId) {
        ReproductiveCycle cycle = getOwnedCycleOrThrow(id, userId);
        cycle.setStatus(ReproductiveStatus.CALVED);
        ReproductiveCycle saved = reproductiveCycleRepository.save(cycle);

        findPhoneNumber(cycle.getFarmer()).ifPresent(phone -> {
            String msg = String.format(
                    "Ranchr Congratulations: Calving recorded for %s! You can register the calf's pedigree and profile on Ranchr.",
                    cycle.getAnimalIdentifier()
            );
            sendSmsSafely(phone, msg);
        });

        return ReproductiveCycleResponseDto.from(saved);
    }

    @Override
    @Transactional
    public void processScheduledAlerts() {
        LocalDate today = LocalDate.now();
        log.info("[Reproduction Scheduler] Running daily cycle check for date: {}", today);

        // 1. Day 21 Non-Return Alerts
        List<ReproductiveCycle> day21Cycles = reproductiveCycleRepository
                .findByStatusAndNonReturnCheckDateLessThanEqualAndDay21AlertSentFalse(
                        ReproductiveStatus.INSEMINATED, today);

        for (ReproductiveCycle cycle : day21Cycles) {
            findPhoneNumber(cycle.getFarmer()).ifPresent(phone -> {
                String msg = String.format(
                        "Ranchr Alert: 21-day non-return check for %s. Has she returned to heat? Reply 1 for YES (book repeat AI), or 2 for NO (conception likely).",
                        cycle.getAnimalIdentifier()
                );
                sendSmsSafely(phone, msg);
            });

            pushWebSocketNotification(cycle.getFarmer().getUserId().toString(), Map.of(
                    "type", "DAY_21_NON_RETURN_CHECK",
                    "cycleId", cycle.getId().toString(),
                    "animalIdentifier", cycle.getAnimalIdentifier()
            ));

            cycle.setDay21AlertSent(true);
            reproductiveCycleRepository.save(cycle);
        }

        // 2. Day 60 Pregnancy Diagnosis Alerts
        List<ReproductiveCycle> day60Cycles = reproductiveCycleRepository
                .findByStatusAndPregnancyDiagnosisDateLessThanEqualAndDay60AlertSentFalse(
                        ReproductiveStatus.INSEMINATED, today);

        for (ReproductiveCycle cycle : day60Cycles) {
            findPhoneNumber(cycle.getFarmer()).ifPresent(phone -> {
                String vetName = cycle.getVetProfile() != null
                        ? "Dr. " + cycle.getVetProfile().getKvbRegisteredName()
                        : "a verified vet";

                String msg = String.format(
                        "Ranchr Alert: Day 60 reached for %s. Schedule a Pregnancy Diagnosis (PD) check with %s on Ranchr to verify gestation.",
                        cycle.getAnimalIdentifier(),
                        vetName
                );
                sendSmsSafely(phone, msg);
            });

            cycle.setDay60AlertSent(true);
            reproductiveCycleRepository.save(cycle);
        }

        // 3. Day 280 Calving Alerts
        List<ReproductiveCycle> calvingCycles = reproductiveCycleRepository
                .findByStatusAndExpectedCalvingDateLessThanEqualAndCalvingAlertSentFalse(
                        ReproductiveStatus.CONFIRMED_PREGNANT, today);

        for (ReproductiveCycle cycle : calvingCycles) {
            findPhoneNumber(cycle.getFarmer()).ifPresent(phone -> {
                String msg = String.format(
                        "Ranchr Alert: Expected calving date for %s is approaching! Prepare maternity pen and ensure clean bedding. Report birth on Ranchr.",
                        cycle.getAnimalIdentifier()
                );
                sendSmsSafely(phone, msg);
            });

            cycle.setCalvingAlertSent(true);
            reproductiveCycleRepository.save(cycle);
        }
    }

    private LocalDate calculateNonReturnDate(String species, LocalDate insemDate) {
        if (species != null && species.equalsIgnoreCase("sheep")) {
            return insemDate.plusDays(17);
        }
        return insemDate.plusDays(21);
    }

    private LocalDate calculatePregnancyDiagnosisDate(String species, LocalDate insemDate) {
        if (species != null && (species.equalsIgnoreCase("goat") || species.equalsIgnoreCase("sheep"))) {
            return insemDate.plusDays(50);
        }
        return insemDate.plusDays(60);
    }

    private LocalDate calculateExpectedCalvingDate(String species, LocalDate insemDate) {
        if (species != null && (species.equalsIgnoreCase("goat") || species.equalsIgnoreCase("sheep"))) {
            return insemDate.plusDays(150);
        }
        return insemDate.plusDays(282);
    }

    private ReproductiveCycle getOwnedCycleOrThrow(UUID id, UUID userId) {
        ReproductiveCycle cycle = reproductiveCycleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reproductive cycle not found: " + id));

        boolean isFarmer = cycle.getFarmer().getUserId().equals(userId);
        boolean isVet = cycle.getVetProfile() != null && cycle.getVetProfile().getVetId().equals(userId);

        if (!isFarmer && !isVet) {
            throw new ForbiddenException("You are not authorized to update this reproductive cycle.");
        }
        return cycle;
    }

    private Optional<String> findPhoneNumber(User user) {
        if (user == null) return Optional.empty();
        return kycRepository.findByUser(user)
                .map(Kyc::getPhoneNumber)
                .filter(p -> p != null && !p.isBlank());
    }

    private void sendSmsSafely(String to, String message) {
        try {
            smsService.send(to, message);
            log.info("[Reproduction SMS] Dispatched to {}: {}", to, message);
        } catch (Exception ex) {
            log.warn("[Reproduction SMS] Failed to send SMS to {}: {}", to, ex.getMessage());
        }
    }

    private void pushWebSocketNotification(String userId, Object payload) {
        try {
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, (Object) payload);
            }
        } catch (Exception ex) {
            log.warn("[WebSocket] Failed to publish notification to user {}: {}", userId, ex.getMessage());
        }
    }
}
