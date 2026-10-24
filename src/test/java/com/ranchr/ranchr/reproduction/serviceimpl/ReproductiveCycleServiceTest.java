package com.ranchr.ranchr.reproduction.serviceimpl;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import com.ranchr.ranchr.reproduction.dto.CreateReproductiveCycleDto;
import com.ranchr.ranchr.reproduction.dto.ReproductiveCycleResponseDto;
import com.ranchr.ranchr.reproduction.model.ReproductiveCycle;
import com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus;
import com.ranchr.ranchr.reproduction.repository.ReproductiveCycleRepository;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReproductiveCycleServiceTest {

    @Mock
    private ReproductiveCycleRepository reproductiveCycleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycRepository kycRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ReproductiveCycleServiceImpl reproductiveCycleService;

    private User farmer;
    private VetProfile vetProfile;
    private UUID farmerId;
    private UUID cycleId;

    @BeforeEach
    void setUp() {
        farmerId = UUID.randomUUID();
        cycleId = UUID.randomUUID();

        farmer = User.builder()
                .userId(farmerId)
                .username("farmer_dan")
                .email("dan@ranch.com")
                .role(Role.USER)
                .build();

        vetProfile = VetProfile.builder()
                .vetId(UUID.randomUUID())
                .kvbRegisteredName("Dr. Jane Vet")
                .build();
    }

    @Test
    void testCreateCycle_CattleCalculatesCorrectMilestones() {
        when(userRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(i -> {
            ReproductiveCycle c = i.getArgument(0);
            c.setId(cycleId);
            return c;
        });

        LocalDate insemDate = LocalDate.of(2026, 10, 1);
        CreateReproductiveCycleDto dto = new CreateReproductiveCycleDto(
                null,
                null,
                null,
                "Cow #101 - Friesian",
                "Cattle",
                "KAGRC-9988",
                insemDate,
                "First artificial insemination"
        );

        ReproductiveCycleResponseDto response = reproductiveCycleService.createCycle(dto, farmerId);

        assertNotNull(response);
        assertEquals(ReproductiveStatus.INSEMINATED, response.status());
        assertEquals(LocalDate.of(2026, 10, 22), response.nonReturnCheckDate()); // +21 days
        assertEquals(LocalDate.of(2026, 11, 30), response.pregnancyDiagnosisDate()); // +60 days
        assertEquals(LocalDate.of(2027, 7, 10), response.expectedCalvingDate()); // +282 days
    }

    @Test
    void testCreateCycleFromServiceRequest_AutoInitializes() {
        ServiceRequest req = new ServiceRequest();
        req.setServiceId(UUID.randomUUID());
        req.setFarmer(farmer);
        req.setVetProfile(vetProfile);
        req.setAnimalSpecies("Dairy Cow");

        when(reproductiveCycleRepository.findByServiceRequest_ServiceId(req.getServiceId()))
                .thenReturn(Optional.empty());
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(i -> i.getArgument(0));

        ReproductiveCycle cycle = reproductiveCycleService.createCycleFromServiceRequest(req, "STRAW-123");

        assertNotNull(cycle);
        assertEquals(ReproductiveStatus.INSEMINATED, cycle.getStatus());
        assertEquals("STRAW-123", cycle.getSemenStrawBatchNumber());
        assertEquals(farmer, cycle.getFarmer());
        verify(reproductiveCycleRepository, times(1)).save(any(ReproductiveCycle.class));
    }

    @Test
    void testProcessScheduledAlerts_DispatchesDay21Alert() {
        LocalDate today = LocalDate.now();

        ReproductiveCycle cycle = ReproductiveCycle.builder()
                .id(cycleId)
                .farmer(farmer)
                .animalIdentifier("Cow #55")
                .status(ReproductiveStatus.INSEMINATED)
                .nonReturnCheckDate(today)
                .day21AlertSent(false)
                .build();

        when(reproductiveCycleRepository.findByStatusAndNonReturnCheckDateLessThanEqualAndDay21AlertSentFalse(
                eq(ReproductiveStatus.INSEMINATED), eq(today)))
                .thenReturn(List.of(cycle));

        when(kycRepository.findByUser(farmer)).thenReturn(Optional.of(
                Kyc.builder().phoneNumber("+254711223344").user(farmer).build()));

        reproductiveCycleService.processScheduledAlerts();

        assertTrue(cycle.isDay21AlertSent());
        verify(smsService, times(1)).send(eq("+254711223344"), contains("21-day non-return check for Cow #55"));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + farmerId), any(Object.class));
        verify(reproductiveCycleRepository, times(1)).save(cycle);
    }

    @Test
    void testConfirmPregnancy_UpdatesStatusAndAlertsFarmer() {
        ReproductiveCycle cycle = ReproductiveCycle.builder()
                .id(cycleId)
                .farmer(farmer)
                .animalIdentifier("Cow #55")
                .status(ReproductiveStatus.INSEMINATED)
                .inseminationDate(LocalDate.now().minusDays(30))
                .nonReturnCheckDate(LocalDate.now().minusDays(9))
                .pregnancyDiagnosisDate(LocalDate.now().plusDays(30))
                .expectedCalvingDate(LocalDate.now().plusMonths(7))
                .build();

        when(reproductiveCycleRepository.findById(cycleId)).thenReturn(Optional.of(cycle));
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(i -> i.getArgument(0));
        when(kycRepository.findByUser(farmer)).thenReturn(Optional.of(
                Kyc.builder().phoneNumber("+254711223344").user(farmer).build()));

        ReproductiveCycleResponseDto response = reproductiveCycleService.confirmPregnancy(cycleId, farmerId);

        assertEquals(ReproductiveStatus.CONFIRMED_PREGNANT, response.status());
        verify(smsService, times(1)).send(eq("+254711223344"), contains("Gestation CONFIRMED"));
    }
}
