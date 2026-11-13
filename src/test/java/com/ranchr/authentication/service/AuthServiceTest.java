package com.ranchr.authentication.service;

import com.ranchr.authentication.dto.AuthResponse;
import com.ranchr.authentication.dto.LoginRequest;
import com.ranchr.authentication.dto.RegisterRequest;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.authentication.security.JwtService;
import com.ranchr.exceptions.DuplicateUserException;
import com.ranchr.exceptions.InvalidCredentialsException;
import com.ranchr.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(UUID.randomUUID())
                .username("farmer_kimani")
                .email("kimani@example.com")
                .password("encoded_secret")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_ShouldRegisterUserSuccessfully_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest("kimani@example.com", "farmer_kimani", "secret123", Role.USER);
        when(userRepository.existsByUsername("farmer_kimani")).thenReturn(false);
        when(userRepository.existsByEmail("kimani@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateAccessToken(eq("farmer_kimani"), any())).thenReturn("mock_access_token");
        when(jwtService.generateRefreshToken("farmer_kimani")).thenReturn("mock_refresh_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock_access_token", response.accessToken());
        assertEquals("mock_refresh_token", response.refreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldRegisterVetSuccessfully_WhenVetRoleRequested() {
        RegisterRequest request = new RegisterRequest("vet@example.com", "dr_maina", "secret123", Role.VET);
        when(userRepository.existsByUsername("dr_maina")).thenReturn(false);
        when(userRepository.existsByEmail("vet@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(eq("dr_maina"), eq(List.of("ROLE_VET")))).thenReturn("vet_access_token");
        when(jwtService.generateRefreshToken("dr_maina")).thenReturn("vet_refresh_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("vet_access_token", response.accessToken());
        assertEquals("vet_refresh_token", response.refreshToken());
    }

    @Test
    void register_ShouldThrowDuplicateUserException_WhenUsernameTaken() {
        RegisterRequest request = new RegisterRequest("kimani@example.com", "farmer_kimani", "secret123", Role.USER);
        when(userRepository.existsByUsername("farmer_kimani")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowDuplicateUserException_WhenEmailTaken() {
        RegisterRequest request = new RegisterRequest("kimani@example.com", "farmer_kimani", "secret123", Role.USER);
        when(userRepository.existsByUsername("farmer_kimani")).thenReturn(false);
        when(userRepository.existsByEmail("kimani@example.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnTokens_WhenValidCredentials() {
        LoginRequest request = new LoginRequest("farmer_kimani", "secret123");
        when(userRepository.findByUsername("farmer_kimani")).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refresh_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowInvalidCredentialsException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown_user", "secret123");
        when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refresh_ShouldReturnNewTokens_WhenValidRefreshToken() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("farmer_kimani")
                .password("encoded_secret")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractUsername("valid_refresh")).thenReturn("farmer_kimani");
        when(userDetailsService.loadUserByUsername("farmer_kimani")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid_refresh", userDetails)).thenReturn(true);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new_access_token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("new_refresh_token");

        AuthResponse response = authService.refresh("valid_refresh");

        assertNotNull(response);
        assertEquals("new_access_token", response.accessToken());
        assertEquals("new_refresh_token", response.refreshToken());
    }

    @Test
    void refresh_ShouldThrowInvalidTokenException_WhenTokenInvalid() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("farmer_kimani")
                .password("encoded_secret")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractUsername("bad_refresh")).thenReturn("farmer_kimani");
        when(userDetailsService.loadUserByUsername("farmer_kimani")).thenReturn(userDetails);
        when(jwtService.isTokenValid("bad_refresh", userDetails)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authService.refresh("bad_refresh"));
    }
}
