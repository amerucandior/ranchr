package com.ranchr.ranchr.connection.listener;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.connection.dto.Events;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.listing.models.Listing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionEventListener {

    private final ConnectionRequestRepository connectionRequestRepository;
    private final KycRepository kycRepository;
    private final SmsService smsService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnectionRequested(Events.ConnectionRequestedEvent event) {
        log.info("[Event] Processing ConnectionRequestedEvent for requestId: {}", event.connectionRequestId());

        connectionRequestRepository.findById(event.connectionRequestId()).ifPresent(request -> {
            Listing listing = request.getListing();
            User owner = listing.getOwner();
            User interested = request.getInterestedUser();

            // 1. Send SMS to Listing Owner via Africa's Talking
            findPhoneNumber(owner).ifPresent(phone -> {
                String animalDesc = listing.getBreed() != null && !listing.getBreed().isBlank()
                        ? listing.getAnimalType() + " (" + listing.getBreed() + ")"
                        : listing.getAnimalType();

                String message = String.format(
                        "Ranchr Alert: %s sent a breeding request for your %s in %s. Check Ranchr to respond.",
                        interested.getUsername(),
                        animalDesc,
                        listing.getLocation()
                );

                sendSmsSafely(phone, message);
            });

            // 2. Real-time WebSocket Push Notification to Owner
            pushWebSocketNotification(owner.getUserId().toString(), Map.of(
                    "type", "CONNECTION_REQUESTED",
                    "requestId", request.getId().toString(),
                    "listingId", listing.getId().toString(),
                    "fromUser", interested.getUsername(),
                    "animalType", listing.getAnimalType()
            ));
        });
    }

    @EventListener
    public void handleConnectionApproved(Events.ConnectionApprovedEvent event) {
        log.info("[Event] Processing ConnectionApprovedEvent for requestId: {}", event.connectionRequestId());

        connectionRequestRepository.findById(event.connectionRequestId()).ifPresent(request -> {
            Listing listing = request.getListing();
            User owner = listing.getOwner();
            User interested = request.getInterestedUser();

            Optional<String> ownerPhone = findPhoneNumber(owner);
            String ownerContact = ownerPhone.orElse(owner.getEmail());

            // 1. Send SMS to Interested User with Owner Contact Details
            findPhoneNumber(interested).ifPresent(phone -> {
                String animalDesc = listing.getBreed() != null && !listing.getBreed().isBlank()
                        ? listing.getAnimalType() + " (" + listing.getBreed() + ")"
                        : listing.getAnimalType();

                String message = String.format(
                        "Ranchr Alert: Your connection for %s was ACCEPTED by %s! Contact: %s to coordinate breeding or AI service.",
                        animalDesc,
                        owner.getUsername(),
                        ownerContact
                );

                sendSmsSafely(phone, message);
            });

            // 2. Real-time WebSocket Push Notification to Interested User
            pushWebSocketNotification(interested.getUserId().toString(), Map.of(
                    "type", "CONNECTION_ACCEPTED",
                    "requestId", request.getId().toString(),
                    "listingId", listing.getId().toString(),
                    "ownerUsername", owner.getUsername(),
                    "contactPhone", ownerPhone.orElse("")
            ));
        });
    }

    @EventListener
    public void handleConnectionRejected(Events.ConnectionRejectedEvent event) {
        log.info("[Event] Processing ConnectionRejectedEvent for requestId: {}", event.connectionRequestId());

        connectionRequestRepository.findById(event.connectionRequestId()).ifPresent(request -> {
            Listing listing = request.getListing();
            User interested = request.getInterestedUser();

            // 1. Send SMS update to Interested User
            findPhoneNumber(interested).ifPresent(phone -> {
                String message = String.format(
                        "Ranchr Update: Your breeding request for %s was declined by the owner.",
                        listing.getAnimalType()
                );
                sendSmsSafely(phone, message);
            });

            // 2. Real-time WebSocket Push Notification
            pushWebSocketNotification(interested.getUserId().toString(), Map.of(
                    "type", "CONNECTION_REJECTED",
                    "requestId", request.getId().toString(),
                    "listingId", listing.getId().toString()
            ));
        });
    }

    private Optional<String> findPhoneNumber(User user) {
        return kycRepository.findByUser(user)
                .map(Kyc::getPhoneNumber)
                .filter(p -> p != null && !p.isBlank());
    }

    private void sendSmsSafely(String phoneNumber, String message) {
        try {
            smsService.send(phoneNumber, message);
            log.info("[SMS] Successfully dispatched Africa's Talking alert to {}", phoneNumber);
        } catch (Exception ex) {
            log.warn("[SMS] Could not deliver SMS to {}: {}", phoneNumber, ex.getMessage());
        }
    }

    private void pushWebSocketNotification(String userId, Object payload) {
        try {
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
            }
        } catch (Exception ex) {
            log.warn("[WebSocket] Failed to publish notification to user {}: {}", userId, ex.getMessage());
        }
    }
}
