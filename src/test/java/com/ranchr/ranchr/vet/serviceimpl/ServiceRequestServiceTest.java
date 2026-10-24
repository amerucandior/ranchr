package com.ranchr.ranchr.vet.serviceimpl;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private VetProfileRepository vetProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycRepository kycRepository;

    @Mock
    private VetRatingRepository vetRatingRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ServiceRequestServiceImpl serviceRequestService;

    private UUID farmerId;
    private UUID vetId;
    private User farmer;
    private User vetUser;
    private VetProfile vetProfile;
    private ServiceRequest request;

    @BeforeEach
    void setUp() {
        farmerId = UUID.randomUUID();
        vetId = UUID.randomUUID();

        farmer = User.builder().userId(farmerId).username("farmer_dan").email("dan@farm.com").role(Role.USER).build();
        vetUser = User.builder().userId(vetId).username("dr_vet").email("dr@vet.com").role(Role.VET).build();

        vetProfile = VetProfile.builder()
                .vetId(vetId)
                .vet(vetUser)
                .kvbRegisteredName("Dr. Jane Vet")
                .status(VerificationStatus.APPROVED)
                .verified(VerificationStatus.APPROVED)
                .available(Available.AVAILABLE)
                .yearsExperience(7)
                .build();

        request = new ServiceRequest();
        request.setServiceId(UUID.randomUUID());
        request.setFarmer(farmer);
        request.setAnimalSpecies("Dairy Cow");
        request.setSymptoms("Standing heat for 4 hours");
        request.setLocation("Naivasha");
        request.setStatus(ServiceStatus.PENDING);
    }

    @Test
    void testCreateRequest_TargetedWithHeatAlert() {
        when(userRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
        when(vetProfileRepository.findByVetId(vetId)).thenReturn(Optional.of(vetProfile));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(i -> {
            ServiceRequest sr = i.getArgument(0);
            sr.setServiceId(UUID.randomUUID());
            return sr;
        });

        when(kycRepository.findByUser(vetUser)).thenReturn(Optional.of(
                Kyc.builder().phoneNumber("+254711223344").user(vetUser).build()));

        CreateServiceRequestDto dto = new CreateServiceRequestDto(
                vetId,
                "Dairy Cow",
                "Standing heat for 4 hours",
                "Ready for AI with Friesian semen",
                "Naivasha",
                null,
                true // isHeatAlert = true
        );

        ServiceRequestResponseDto response = serviceRequestService.createRequest(dto, farmerId);

        assertNotNull(response);
        assertEquals(ServiceStatus.PENDING, response.status());
        verify(smsService, atLeastOnce()).send(eq("+254711223344"), contains("🚨 URGENT HEAT ALERT"));
    }

    @Test
    void testAcceptRequest_SetsAcceptedAndVetBusy() {
        UUID serviceId = request.getServiceId();
        when(vetProfileRepository.findByVetId(vetId)).thenReturn(Optional.of(vetProfile));
        when(serviceRequestRepository.findById(serviceId)).thenReturn(Optional.of(request));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(i -> i.getArgument(0));

        when(kycRepository.findByUser(farmer)).thenReturn(Optional.of(
                Kyc.builder().phoneNumber("+254722334455").user(farmer).build()));
        when(kycRepository.findByUser(vetUser)).thenReturn(Optional.of(
                Kyc.builder().phoneNumber("+254711223344").user(vetUser).build()));

        ServiceRequestResponseDto response = serviceRequestService.acceptRequest(serviceId, vetId);

        assertEquals(ServiceStatus.ACCEPTED, response.status());
        assertEquals(Available.BUSY, vetProfile.getAvailable());
        assertNotNull(request.getAcceptedAt());
        verify(smsService, times(1)).send(eq("+254722334455"), contains("Dr. Jane Vet has ACCEPTED"));
    }

    @Test
    void testCompleteService_LogsBatchNumberAndRestoresAvailable() {
        UUID serviceId = request.getServiceId();
        request.setVetProfile(vetProfile);
        request.setStatus(ServiceStatus.IN_PROGRESS);
        vetProfile.setAvailable(Available.BUSY);

        when(serviceRequestRepository.findById(serviceId)).thenReturn(Optional.of(request));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(i -> i.getArgument(0));

        CompleteServiceRequestDto completionDto = new CompleteServiceRequestDto(
                "KAGRC-FRIESIAN-9872",
                "Insemination successful",
                null,
                "Watch for return to heat in 21 days"
        );

        ServiceRequestResponseDto response = serviceRequestService.completeService(serviceId, completionDto, vetId);

        assertEquals(ServiceStatus.COMPLETED, response.status());
        assertEquals(Available.AVAILABLE, vetProfile.getAvailable());
        assertNotNull(request.getCompletedAt());
        assertTrue(request.getDescription().contains("KAGRC-FRIESIAN-9872"));
    }

    @Test
    void testRateService_SavesRating() {
        UUID serviceId = request.getServiceId();
        request.setVetProfile(vetProfile);
        request.setStatus(ServiceStatus.COMPLETED);

        when(serviceRequestRepository.findById(serviceId)).thenReturn(Optional.of(request));
        when(vetRatingRepository.existsByService_ServiceId(serviceId)).thenReturn(false);
        when(vetRatingRepository.save(any(VetRating.class))).thenAnswer(i -> {
            VetRating r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        CreateRatingDto ratingDto = new CreateRatingDto(5, "Prompt arrival, cow settled smoothly!");
        VetRatingResponseDto response = serviceRequestService.rateService(serviceId, ratingDto, farmerId);

        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Prompt arrival, cow settled smoothly!", response.comment());
        verify(vetRatingRepository, times(1)).save(any(VetRating.class));
    }
}
