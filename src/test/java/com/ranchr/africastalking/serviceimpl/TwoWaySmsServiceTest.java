package com.ranchr.africastalking.serviceimpl;

import com.ranchr.africastalking.dto.InboundSmsDto;
import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.connection.service.ConnectionRequestService;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.vet.dto.CompleteServiceRequestDto;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import com.ranchr.ranchr.vet.repository.ServiceRequestRepository;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import com.ranchr.ranchr.vet.service.ServiceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoWaySmsServiceTest {

    @Mock
    private KycRepository kycRepository;

    @Mock
    private VetProfileRepository vetProfileRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private ServiceRequestService serviceRequestService;

    @Mock
    private ConnectionRequestService connectionRequestService;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private TwoWaySmsServiceImpl twoWaySmsService;

    private User vetUser;
    private VetProfile vetProfile;
    private Kyc vetKyc;

    @BeforeEach
    void setUp() {
        vetUser = User.builder()
                .userId(UUID.randomUUID())
                .username("dr_vet")
                .role(Role.VET)
                .email("vet@ranchr.com")
                .build();

        vetProfile = VetProfile.builder()
                .vetId(vetUser.getUserId())
                .vet(vetUser)
                .kvbRegisteredName("Dr. Jane Vet")
                .build();

        vetKyc = Kyc.builder()
                .user(vetUser)
                .phoneNumber("+254711223344")
                .build();
    }

    @Test
    void testProcessInboundSms_UnregisteredSender() {
        when(kycRepository.findByPhoneNumberEndingWith(anyString())).thenReturn(Optional.empty());
        when(kycRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

        InboundSmsDto sms = new InboundSmsDto("+254700000000", "20880", "ACCEPT", null, "id123", null);
        String reply = twoWaySmsService.processInboundSms(sms);

        assertTrue(reply.contains("Your number is not registered"));
        verify(smsService, times(1)).send(eq("+254700000000"), contains("not registered"));
    }

    @Test
    void testProcessInboundSms_VetAcceptsServiceViaSms() {
        when(kycRepository.findByPhoneNumberEndingWith(eq("711223344"))).thenReturn(Optional.of(vetKyc));
        when(vetProfileRepository.findByVetId(vetUser.getUserId())).thenReturn(Optional.of(vetProfile));

        ServiceRequest serviceReq = new ServiceRequest();
        serviceReq.setServiceId(UUID.randomUUID());
        serviceReq.setAnimalSpecies("Dairy Heifer");
        serviceReq.setLocation("Njoro");
        serviceReq.setFarmer(User.builder().username("farmer_bob").build());

        when(serviceRequestRepository.findTopByVetProfileAndStatusOrderByCreatedAtDesc(vetProfile, ServiceStatus.PENDING))
                .thenReturn(Optional.of(serviceReq));

        InboundSmsDto sms = new InboundSmsDto("+254711223344", "20880", "1", null, "id456", null);
        String reply = twoWaySmsService.processInboundSms(sms);

        assertTrue(reply.contains("ACCEPTED service for Dairy Heifer"));
        verify(serviceRequestService, times(1)).acceptRequest(serviceReq.getServiceId(), vetUser.getUserId());
        verify(smsService, times(1)).send(eq("+254711223344"), contains("ACCEPTED service for Dairy Heifer"));
    }

    @Test
    void testProcessInboundSms_VetCompletesServiceWithStrawBatch() {
        when(kycRepository.findByPhoneNumberEndingWith(eq("711223344"))).thenReturn(Optional.of(vetKyc));
        when(vetProfileRepository.findByVetId(vetUser.getUserId())).thenReturn(Optional.of(vetProfile));

        ServiceRequest serviceReq = new ServiceRequest();
        serviceReq.setServiceId(UUID.randomUUID());
        serviceReq.setAnimalSpecies("Dairy Heifer");

        when(serviceRequestRepository.findTopByVetProfileAndStatusOrderByCreatedAtDesc(vetProfile, ServiceStatus.IN_PROGRESS))
                .thenReturn(Optional.of(serviceReq));

        InboundSmsDto sms = new InboundSmsDto("+254711223344", "20880", "COMPLETE KAGRC-9988", null, "id789", null);
        String reply = twoWaySmsService.processInboundSms(sms);

        assertTrue(reply.contains("COMPLETED"));
        assertTrue(reply.contains("KAGRC-9988"));
        verify(serviceRequestService, times(1)).completeService(eq(serviceReq.getServiceId()), any(CompleteServiceRequestDto.class), eq(vetUser.getUserId()));
    }

    @Test
    void testProcessInboundSms_HelpCommand() {
        when(kycRepository.findByPhoneNumberEndingWith(eq("711223344"))).thenReturn(Optional.of(vetKyc));

        InboundSmsDto sms = new InboundSmsDto("+254711223344", "20880", "HELP", null, "id999", null);
        String reply = twoWaySmsService.processInboundSms(sms);

        assertTrue(reply.contains("Ranchr SMS: Reply 1 or ACCEPT"));
        verify(smsService, times(1)).send(eq("+254711223344"), contains("Ranchr SMS"));
    }
}
