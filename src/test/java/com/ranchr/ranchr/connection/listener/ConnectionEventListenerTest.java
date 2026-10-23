package com.ranchr.ranchr.connection.listener;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.connection.dto.Events;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.listing.models.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionEventListenerTest {

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private KycRepository kycRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ConnectionEventListener listener;

    private User owner;
    private User interested;
    private Listing listing;
    private ConnectionRequest request;
    private UUID requestId;

    @BeforeEach
    void setUp() {
        listener = new ConnectionEventListener(
                connectionRequestRepository,
                kycRepository,
                smsService,
                messagingTemplate
        );

        requestId = UUID.randomUUID();
        owner = User.builder().userId(UUID.randomUUID()).username("farmer_john").email("john@example.com").build();
        interested = User.builder().userId(UUID.randomUUID()).username("breeder_mary").email("mary@example.com").build();

        listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setOwner(owner);
        listing.setAnimalType("Cow");
        listing.setBreed("Friesian");
        listing.setLocation("Nakuru");

        request = ConnectionRequest.builder()
                .id(requestId)
                .listing(listing)
                .interestedUser(interested)
                .build();
    }

    @Test
    void testHandleConnectionRequested_DispatchesSmsAndWebSocket() {
        when(connectionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Kyc ownerKyc = Kyc.builder().phoneNumber("+254712345678").user(owner).build();
        when(kycRepository.findByUser(owner)).thenReturn(Optional.of(ownerKyc));

        listener.handleConnectionRequested(new Events.ConnectionRequestedEvent(requestId));

        verify(smsService, times(1)).send(eq("+254712345678"), contains("breeder_mary sent a breeding request"));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + owner.getUserId()), any(Object.class));
    }

    @Test
    void testHandleConnectionApproved_DispatchesSmsToInterestedUser() {
        when(connectionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Kyc interestedKyc = Kyc.builder().phoneNumber("+254798765432").user(interested).build();
        when(kycRepository.findByUser(interested)).thenReturn(Optional.of(interestedKyc));

        Kyc ownerKyc = Kyc.builder().phoneNumber("+254712345678").user(owner).build();
        when(kycRepository.findByUser(owner)).thenReturn(Optional.of(ownerKyc));

        listener.handleConnectionApproved(new Events.ConnectionApprovedEvent(requestId));

        verify(smsService, times(1)).send(eq("+254798765432"), contains("ACCEPTED by farmer_john"));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + interested.getUserId()), any(Object.class));
    }
}
