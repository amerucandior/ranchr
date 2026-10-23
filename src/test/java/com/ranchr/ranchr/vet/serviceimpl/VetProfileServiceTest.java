package com.ranchr.ranchr.vet.serviceimpl;

import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.cloudinary.service.CloudinaryService;
import com.ranchr.ranchr.vet.dto.CreateVetProfileRequest;
import com.ranchr.ranchr.vet.dto.PresenceEvent;
import com.ranchr.ranchr.vet.dto.VerficationRequest;
import com.ranchr.ranchr.vet.dto.VetProfileResponseDto;
import com.ranchr.ranchr.vet.dto.enums.PresenceEventType;
import com.ranchr.ranchr.vet.dto.enums.Verify;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.ServicesOffered;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import com.ranchr.ranchr.vet.repository.VetProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VetProfileServiceTest {

    @Mock
    private VetProfileRepository vetProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private VetProfileServiceImpl vetProfileService;

    private UUID vetId;
    private User user;
    private VetProfile profile;

    @BeforeEach
    void setUp() {
        vetId = UUID.randomUUID();
        user = User.builder()
                .userId(vetId)
                .username("dr_vet")
                .email("drvet@example.com")
                .role(Role.USER)
                .build();

        profile = VetProfile.builder()
                .vetId(vetId)
                .vet(user)
                .available(Available.OFFLINE)
                .status(VerificationStatus.UNDER_REVIEW)
                .verified(VerificationStatus.UNDER_REVIEW)
                .yearsExperience(5)
                .kvbRegisteredName("Dr. Vet")
                .kvbLicenseNumber("KVB/12345")
                .build();
    }

    @Test
    void testToggleAvailability_GoOnlineSetsAvailable() {
        when(vetProfileRepository.findByVetId(vetId)).thenReturn(Optional.of(profile));

        PresenceEvent event = new PresenceEvent();
        ReflectionTestUtils.setField(event, "type", PresenceEventType.GO_ONLINE);

        vetProfileService.toggleAvailability(event, vetId);

        assertEquals(Available.AVAILABLE, profile.getAvailable());
        verify(vetProfileRepository, times(1)).save(profile);
    }

    @Test
    void testToggleAvailability_GoOfflineSetsOffline() {
        profile.setAvailable(Available.AVAILABLE);
        when(vetProfileRepository.findByVetId(vetId)).thenReturn(Optional.of(profile));

        PresenceEvent event = new PresenceEvent();
        ReflectionTestUtils.setField(event, "type", PresenceEventType.GO_OFFLINE);

        vetProfileService.toggleAvailability(event, vetId);

        assertEquals(Available.OFFLINE, profile.getAvailable());
        verify(vetProfileRepository, times(1)).save(profile);
    }

    @Test
    void testVerifyVetProfile_AcceptSetsApproved() {
        when(vetProfileRepository.findByVetId(vetId)).thenReturn(Optional.of(profile));

        VerficationRequest request = new VerficationRequest(Verify.ACCEPT);
        vetProfileService.verifyVetProfile(request, vetId);

        assertEquals(VerificationStatus.APPROVED, profile.getStatus());
        assertEquals(VerificationStatus.APPROVED, profile.getVerified());
        assertNotNull(profile.getVerifiedAt());
        verify(vetProfileRepository, times(1)).save(profile);
    }

    @Test
    void testEnlistVetProfile_PromotesUserRoleAndSaves() {
        when(userRepository.findById(vetId)).thenReturn(Optional.of(user));
        when(vetProfileRepository.findByVetId(vetId)).thenReturn(Optional.empty());
        when(vetProfileRepository.save(any(VetProfile.class))).thenAnswer(i -> i.getArgument(0));

        CreateVetProfileRequest request = new CreateVetProfileRequest(
                Set.of(ServicesOffered.ARTIFICIAL_INSEMINATION),
                4
        );

        VetProfileResponseDto response = vetProfileService.enlistVetProfile(request, vetId);

        assertNotNull(response);
        assertEquals(Role.VET, user.getRole());
        verify(userRepository, times(1)).save(user);
        verify(vetProfileRepository, times(1)).save(any(VetProfile.class));
    }
}
