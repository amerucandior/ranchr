package com.ranchr.africastalking.serviceimpl;

import com.ranchr.africastalking.dto.InboundSmsDto;
import com.ranchr.africastalking.service.SmsService;
import com.ranchr.africastalking.service.TwoWaySmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.connection.service.ConnectionRequestService;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.vet.dto.CompleteServiceRequestDto;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import com.ranchr.ranchr.vet.repository.ServiceRequestRepository;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import com.ranchr.ranchr.vet.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoWaySmsServiceImpl implements TwoWaySmsService {

    private final KycRepository kycRepository;
    private final VetProfileRepository vetProfileRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ConnectionRequestRepository connectionRequestRepository;
    private final ServiceRequestService serviceRequestService;
    private final ConnectionRequestService connectionRequestService;
    private final SmsService smsService;
    private final com.ranchr.ranchr.reproduction.repository.ReproductiveCycleRepository reproductiveCycleRepository;

    @Override
    @Transactional
    public String processInboundSms(InboundSmsDto sms) {
        String from = sms.from();
        String text = sms.text() != null ? sms.text().trim() : "";
        log.info("[TwoWaySMS] Received inbound message from '{}': '{}'", from, text);

        if (from == null || from.isBlank() || text.isBlank()) {
            return "Ignored empty message";
        }

        Optional<Kyc> kycOpt = findKycByPhone(from);
        if (kycOpt.isEmpty()) {
            String unregisteredMsg = "Ranchr: Your number is not registered. Please sign up on the Ranchr platform to use SMS actions.";
            sendReply(from, unregisteredMsg);
            return unregisteredMsg;
        }

        User user = kycOpt.get().getUser();
        String[] tokens = text.split("\\s+");
        String command = tokens[0].toUpperCase();

        String responseMessage;
        switch (command) {
            case "1":
            case "ACCEPT":
            case "YES":
                responseMessage = handleAccept(user, tokens);
                break;

            case "START":
            case "ENROUTE":
            case "ONWAY":
                responseMessage = handleStart(user);
                break;

            case "DONE":
            case "COMPLETE":
                responseMessage = handleComplete(user, tokens);
                break;

            case "2":
            case "REJECT":
            case "DECLINE":
            case "NO":
                responseMessage = handleReject(user);
                break;

            case "STATUS":
                responseMessage = handleStatus(user);
                break;

            case "HELP":
            default:
                responseMessage = "Ranchr SMS: Reply 1 or ACCEPT to confirm job, START when en route, COMPLETE <batch> when done, or STATUS to check active jobs.";
                break;
        }

        sendReply(from, responseMessage);
        return responseMessage;
    }

    private String handleAccept(User user, String[] tokens) {
        // 1. Check if user is a registered Vet
        Optional<VetProfile> vetOpt = vetProfileRepository.findByVetId(user.getUserId());
        if (vetOpt.isPresent()) {
            VetProfile vet = vetOpt.get();

            // Look for targeted pending request first
            Optional<ServiceRequest> pendingReq = serviceRequestRepository
                    .findTopByVetProfileAndStatusOrderByCreatedAtDesc(vet, ServiceStatus.PENDING);

            // If no targeted request, look for latest pending broadcast
            if (pendingReq.isEmpty()) {
                pendingReq = serviceRequestRepository
                        .findTopByVetProfileIsNullAndStatusOrderByCreatedAtDesc(ServiceStatus.PENDING);
            }

            if (pendingReq.isPresent()) {
                ServiceRequest req = pendingReq.get();
                try {
                    serviceRequestService.acceptRequest(req.getServiceId(), vet.getVetId());
                    return String.format(
                            "Ranchr: You ACCEPTED service for %s in %s. Farmer: %s. Please proceed to farm.",
                            req.getAnimalSpecies(),
                            req.getLocation(),
                            req.getFarmer().getUsername()
                    );
                } catch (Exception ex) {
                    return "Ranchr: Could not accept request: " + ex.getMessage();
                }
            }
        }

        // 2. Check if user is a Breeder/Owner with a pending connection request
        Optional<ConnectionRequest> pendingConn = connectionRequestRepository
                .findTopByListing_OwnerAndStatusOrderByCreatedAtDesc(user, ConnectionStatus.PENDING);

        if (pendingConn.isPresent()) {
            ConnectionRequest conn = pendingConn.get();
            try {
                connectionRequestService.accept(conn.getId(), user);
                return String.format(
                        "Ranchr: You ACCEPTED %s's connection request for %s! Coordinate via Ranchr.",
                        conn.getInterestedUser().getUsername(),
                        conn.getListing().getAnimalType()
                );
            } catch (Exception ex) {
                return "Ranchr: Could not accept connection: " + ex.getMessage();
            }
        }

        // 3. Check if user is a Farmer responding to a Day 21 Non-Return check (Heat Returned)
        Optional<com.ranchr.ranchr.reproduction.model.ReproductiveCycle> cycleOpt = reproductiveCycleRepository
                .findTopByFarmerAndStatusOrderByCreatedAtDesc(user, com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus.INSEMINATED);

        if (cycleOpt.isPresent() && cycleOpt.get().isDay21AlertSent()) {
            com.ranchr.ranchr.reproduction.model.ReproductiveCycle cycle = cycleOpt.get();
            cycle.setStatus(com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus.HEAT_RETURNED);
            reproductiveCycleRepository.save(cycle);
            return String.format(
                    "Ranchr: Recorded return to heat for %s. A repeat AI request has been flagged on your dashboard.",
                    cycle.getAnimalIdentifier()
            );
        }

        return "Ranchr: No pending service, connection request, or Day 21 alert found to accept.";
    }

    private String handleStart(User user) {
        Optional<VetProfile> vetOpt = vetProfileRepository.findByVetId(user.getUserId());
        if (vetOpt.isEmpty()) {
            return "Ranchr: START command is only available for veterinary officers.";
        }

        VetProfile vet = vetOpt.get();
        Optional<ServiceRequest> activeReq = serviceRequestRepository
                .findTopByVetProfileAndStatusOrderByCreatedAtDesc(vet, ServiceStatus.ACCEPTED);

        if (activeReq.isPresent()) {
            ServiceRequest req = activeReq.get();
            try {
                serviceRequestService.startService(req.getServiceId(), vet.getVetId());
                return String.format(
                        "Ranchr: Service for %s marked IN_PROGRESS. Farmer %s has been notified.",
                        req.getAnimalSpecies(),
                        req.getFarmer().getUsername()
                );
            } catch (Exception ex) {
                return "Ranchr: Could not start service: " + ex.getMessage();
            }
        }

        return "Ranchr: No ACCEPTED service found to start.";
    }

    private String handleComplete(User user, String[] tokens) {
        Optional<VetProfile> vetOpt = vetProfileRepository.findByVetId(user.getUserId());
        if (vetOpt.isEmpty()) {
            return "Ranchr: COMPLETE command is only available for veterinary officers.";
        }

        VetProfile vet = vetOpt.get();
        // Look for request that is IN_PROGRESS or ACCEPTED
        Optional<ServiceRequest> reqOpt = serviceRequestRepository
                .findTopByVetProfileAndStatusOrderByCreatedAtDesc(vet, ServiceStatus.IN_PROGRESS);

        if (reqOpt.isEmpty()) {
            reqOpt = serviceRequestRepository
                    .findTopByVetProfileAndStatusOrderByCreatedAtDesc(vet, ServiceStatus.ACCEPTED);
        }

        if (reqOpt.isPresent()) {
            ServiceRequest req = reqOpt.get();
            String strawBatch = (tokens.length > 1) ? tokens[1] : null;

            try {
                CompleteServiceRequestDto compDto = new CompleteServiceRequestDto(
                        strawBatch,
                        "Completed via SMS",
                        null,
                        null
                );
                serviceRequestService.completeService(req.getServiceId(), compDto, vet.getVetId());

                String batchMsg = (strawBatch != null) ? " (Semen Straw: " + strawBatch + ")" : "";
                return String.format(
                        "Ranchr: Service marked COMPLETED for %s%s. Farmer has been notified.",
                        req.getAnimalSpecies(),
                        batchMsg
                );
            } catch (Exception ex) {
                return "Ranchr: Could not complete service: " + ex.getMessage();
            }
        }

        return "Ranchr: No active service found to complete.";
    }

    private String handleReject(User user) {
        Optional<ConnectionRequest> pendingConn = connectionRequestRepository
                .findTopByListing_OwnerAndStatusOrderByCreatedAtDesc(user, ConnectionStatus.PENDING);

        if (pendingConn.isPresent()) {
            ConnectionRequest conn = pendingConn.get();
            try {
                connectionRequestService.reject(conn.getId(), user);
                return "Ranchr: You declined the connection request.";
            } catch (Exception ex) {
                return "Ranchr: Error declining request: " + ex.getMessage();
            }
        }

        // Check if user is a Farmer confirming no-return to heat (Day 21 milestone)
        Optional<com.ranchr.ranchr.reproduction.model.ReproductiveCycle> cycleOpt = reproductiveCycleRepository
                .findTopByFarmerAndStatusOrderByCreatedAtDesc(user, com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus.INSEMINATED);

        if (cycleOpt.isPresent() && cycleOpt.get().isDay21AlertSent()) {
            com.ranchr.ranchr.reproduction.model.ReproductiveCycle cycle = cycleOpt.get();
            cycle.setStatus(com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus.CONFIRMED_PREGNANT);
            reproductiveCycleRepository.save(cycle);
            return String.format(
                    "Ranchr: Recorded NO return to heat for %s! Presumed pregnant. Day 60 diagnosis reminder scheduled.",
                    cycle.getAnimalIdentifier()
            );
        }

        return "Ranchr: No pending request found to decline.";
    }

    private String handleStatus(User user) {
        Optional<VetProfile> vetOpt = vetProfileRepository.findByVetId(user.getUserId());
        if (vetOpt.isPresent()) {
            VetProfile vet = vetOpt.get();
            long activeCount = serviceRequestRepository.findByVetProfileAndStatus(vet, ServiceStatus.ACCEPTED).size()
                    + serviceRequestRepository.findByVetProfileAndStatus(vet, ServiceStatus.IN_PROGRESS).size();

            return String.format(
                    "Ranchr: Dr. %s, you have %d active service assignments. Availability: %s.",
                    vet.getKvbRegisteredName(),
                    activeCount,
                    vet.getAvailable()
            );
        }

        Optional<ServiceRequest> farmerReq = serviceRequestRepository
                .findTopByFarmerAndStatusOrderByCreatedAtDesc(user, ServiceStatus.PENDING);

        if (farmerReq.isPresent()) {
            return String.format(
                    "Ranchr: Your service request for %s in %s is PENDING vet acceptance.",
                    farmerReq.get().getAnimalSpecies(),
                    farmerReq.get().getLocation()
            );
        }

        return "Ranchr: You have no active pending requests.";
    }

    private Optional<Kyc> findKycByPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) return Optional.empty();
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.length() >= 9) {
            String suffix = digits.substring(digits.length() - 9);
            Optional<Kyc> bySuffix = kycRepository.findByPhoneNumberEndingWith(suffix);
            if (bySuffix.isPresent()) return bySuffix;
        }
        return kycRepository.findByPhoneNumber(rawPhone);
    }

    private void sendReply(String to, String message) {
        try {
            smsService.send(to, message);
            log.info("[TwoWaySMS] Sent SMS reply to {}: '{}'", to, message);
        } catch (Exception ex) {
            log.warn("[TwoWaySMS] Failed to send SMS reply to {}: {}", to, ex.getMessage());
        }
    }
}
