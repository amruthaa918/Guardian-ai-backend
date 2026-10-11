package com.guardianai.service;

import com.guardianai.dto.LoginRequest;
import com.guardianai.dto.LoginResponse;
import com.guardianai.dto.RegisterRequest;
import com.guardianai.entity.User;
import com.guardianai.repository.UserRepository;
import com.guardianai.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "encodedPassword", "9876543210", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should successfully register a new user with hashed password")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("Amrutha", "amrutha@example.com", "123456", "9876543210");
        when(userRepository.existsByEmail("amrutha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashedPassword123");

        assertDoesNotThrow(() -> authService.register(request));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when email is already in use")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Amrutha", "amrutha@example.com", "123456", "9876543210");
        when(userRepository.existsByEmail("amrutha@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertTrue(exception.getMessage().contains("already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login and return JWT token")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("amrutha@example.com", "123456");

        when(userRepository.findByEmail("amrutha@example.com")).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateToken("amrutha@example.com", 1L, "Amrutha")).thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Amrutha", response.getName());
        assertEquals("amrutha@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Should fail login with bad credentials")
    void testLoginBadCredentials() {
        LoginRequest request = new LoginRequest("amrutha@example.com", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
