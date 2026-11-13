package com.ranchr.ranchr.connection.service;

import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.exceptions.BadRequestException;
import com.ranchr.exceptions.ConflictException;
import com.ranchr.exceptions.ForbiddenException;
import com.ranchr.exceptions.NotFoundException;
import com.ranchr.ranchr.connection.dto.Events;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionRequestServiceTest {

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConnectionRequestService connectionRequestService;

    private User owner;
    private User buyer;
    private Listing listing;
    private ConnectionRequest request;
    private UUID requestId;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .userId(UUID.randomUUID())
                .username("farmer_owner")
                .email("owner@example.com")
                .role(Role.USER)
                .build();

        buyer = User.builder()
                .userId(UUID.randomUUID())
                .username("buyer_john")
                .email("buyer@example.com")
                .role(Role.USER)
                .build();

        listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setActivationCycle(1);

        requestId = UUID.randomUUID();
        request = ConnectionRequest.builder()
                .id(requestId)
                .listing(listing)
                .interestedUser(buyer)
                .status(ConnectionStatus.PENDING)
                .message("Interested in purchasing your heifer")
                .listingActivationCycle(1)
                .build();
    }

    @Test
    void create_ShouldSaveRequestAndPublishEvent_WhenValid() {
        when(connectionRequestRepository.existsByListingAndInterestedUserAndStatusIn(any(), any(), any()))
                .thenReturn(false);
        when(connectionRequestRepository.findTopByListingAndInterestedUserAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(connectionRequestRepository.save(any(ConnectionRequest.class)))
                .thenReturn(request);

        ConnectionRequest created = connectionRequestService.create(listing, buyer, "Interested");

        assertNotNull(created);
        assertEquals(requestId, created.getId());
        verify(connectionRequestRepository).save(any(ConnectionRequest.class));
        verify(connectionRequestRepository).incrementInterestCount(listing.getId());
        verify(eventPublisher).publishEvent(any(Events.ConnectionRequestedEvent.class));
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenRequestingOwnListing() {
        assertThrows(BadRequestException.class, () ->
                connectionRequestService.create(listing, owner, "My own cow"));
        verify(connectionRequestRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenListingNotActive() {
        listing.setStatus(ListingStatus.INACTIVE);

        assertThrows(BadRequestException.class, () ->
                connectionRequestService.create(listing, buyer, "Inquire"));
        verify(connectionRequestRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowConflictException_WhenBlockingRequestAlreadyExists() {
        when(connectionRequestRepository.existsByListingAndInterestedUserAndStatusIn(any(), any(), any()))
                .thenReturn(true);

        assertThrows(ConflictException.class, () ->
                connectionRequestService.create(listing, buyer, "Inquire again"));
    }

    @Test
    void accept_ShouldTransitionStatusToAcceptedAndPublishEvent_WhenOwner() {
        when(connectionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        ConnectionRequest accepted = connectionRequestService.accept(requestId, owner);

        assertNotNull(accepted);
        assertEquals(ConnectionStatus.ACCEPTED, accepted.getStatus());
        verify(eventPublisher).publishEvent(any(Events.ConnectionApprovedEvent.class));
    }

    @Test
    void accept_ShouldThrowForbiddenException_WhenNotOwner() {
        when(connectionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(ForbiddenException.class, () ->
                connectionRequestService.accept(requestId, buyer));
    }

    @Test
    void accept_ShouldThrowConflictException_WhenNotPending() {
        request.setStatus(ConnectionStatus.ACCEPTED);
        when(connectionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(ConflictException.class, () ->
                connectionRequestService.accept(requestId, owner));
    }

    @Test
    void reject_ShouldTransitionStatusToRejectedAndPublishEvent_WhenOwner() {
        when(connectionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        ConnectionRequest rejected = connectionRequestService.reject(requestId, owner);

        assertNotNull(rejected);
        assertEquals(ConnectionStatus.REJECTED, rejected.getStatus());
        verify(connectionRequestRepository).decrementInterestCount(listing.getId());
        verify(eventPublisher).publishEvent(any(Events.ConnectionRejectedEvent.class));
    }
}
