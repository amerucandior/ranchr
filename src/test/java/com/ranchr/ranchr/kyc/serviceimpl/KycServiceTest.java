package com.ranchr.ranchr.kyc.serviceimpl;

import com.ranchr.africastalking.service.SmsService;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.exceptions.DuplicateUserException;
import com.ranchr.ranchr.kyc.dto.KycRequest;
import com.ranchr.ranchr.kyc.dto.KycStatusDto;
import com.ranchr.ranchr.kyc.dto.OtpRequest;
import com.ranchr.ranchr.kyc.model.Kyc;
import com.ranchr.ranchr.kyc.otp.service.OtpService;
import com.ranchr.ranchr.kyc.repository.KycRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private SmsService smsService;

    @Mock
    private KycRepository kycRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private KycServiceImpl kycService;

    private User sampleUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(UUID.randomUUID())
                .username("farmer_kimani")
                .email("kimani@example.com")
                .role(Role.USER)
                .build();

        otherUser = User.builder()
                .userId(UUID.randomUUID())
                .username("farmer_other")
                .email("other@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void verify_ShouldGenerateOtpAndSendSms_WhenValidRequest() {
        KycRequest request = new KycRequest("+254711223344", "12345678");
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.empty());
        when(kycRepository.findByNationalId("12345678")).thenReturn(Optional.empty());
        when(kycRepository.findByPhoneNumber("+254711223344")).thenReturn(Optional.empty());
        when(otpService.generateAndStoreOtp(any(Kyc.class))).thenReturn("123456");

        kycService.verify(request);

        verify(kycRepository).save(any(Kyc.class));
        verify(smsService).send(eq("+254711223344"), contains("123456"));
    }

    @Test
    void verify_ShouldThrowDuplicateUserException_WhenAlreadyVerified() {
        KycRequest request = new KycRequest("+254711223344", "12345678");
        Kyc verifiedKyc = Kyc.builder().user(sampleUser).verified(true).build();
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.of(verifiedKyc));

        assertThrows(DuplicateUserException.class, () -> kycService.verify(request));
        verify(smsService, never()).send(any(), any());
    }

    @Test
    void verify_ShouldThrowDuplicateUserException_WhenNationalIdUsedByOtherUser() {
        KycRequest request = new KycRequest("+254711223344", "12345678");
        Kyc otherKyc = Kyc.builder().user(otherUser).nationalId("12345678").build();
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.empty());
        when(kycRepository.findByNationalId("12345678")).thenReturn(Optional.of(otherKyc));

        assertThrows(DuplicateUserException.class, () -> kycService.verify(request));
    }

    @Test
    void verify_ShouldThrowDuplicateUserException_WhenPhoneNumberUsedByOtherUser() {
        KycRequest request = new KycRequest("+254711223344", "12345678");
        Kyc otherKyc = Kyc.builder().user(otherUser).phoneNumber("+254711223344").build();
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.empty());
        when(kycRepository.findByNationalId("12345678")).thenReturn(Optional.empty());
        when(kycRepository.findByPhoneNumber("+254711223344")).thenReturn(Optional.of(otherKyc));

        assertThrows(DuplicateUserException.class, () -> kycService.verify(request));
    }

    @Test
    void verifyOtp_ShouldVerifyUser_WhenOtpValid() {
        OtpRequest request = new OtpRequest("654321");
        Kyc pendingKyc = Kyc.builder().user(sampleUser).verified(false).build();
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.of(pendingKyc));

        kycService.verifyOtp(request);

        verify(otpService).validateAndClear(pendingKyc, "654321");
        assertTrue(pendingKyc.isVerified());
        assertNotNull(pendingKyc.getVerifiedAt());
        verify(kycRepository).save(pendingKyc);
    }

    @Test
    void verifyOtp_ShouldThrowUsernameNotFoundException_WhenNoPendingKyc() {
        OtpRequest request = new OtpRequest("654321");
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> kycService.verifyOtp(request));
    }

    @Test
    void getKycStatus_ShouldReturnVerifiedStatus_WhenRecordExists() {
        LocalDateTime now = LocalDateTime.now();
        Kyc kyc = Kyc.builder()
                .user(sampleUser)
                .phoneNumber("+254711223344")
                .nationalId("12345678")
                .verified(true)
                .verifiedAt(now)
                .build();
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.of(kyc));

        KycStatusDto status = kycService.getKycStatus();

        assertNotNull(status);
        assertTrue(status.verified());
        assertEquals("+254711223344", status.phoneNumber());
        assertEquals("12345678", status.nationalId());
        assertEquals(now, status.verifiedAt());
    }

    @Test
    void getKycStatus_ShouldReturnUnverified_WhenNoRecordExists() {
        when(currentUser.get()).thenReturn(sampleUser);
        when(kycRepository.findByUser(sampleUser)).thenReturn(Optional.empty());

        KycStatusDto status = kycService.getKycStatus();

        assertNotNull(status);
        assertFalse(status.verified());
        assertNull(status.phoneNumber());
        assertNull(status.nationalId());
    }
}
