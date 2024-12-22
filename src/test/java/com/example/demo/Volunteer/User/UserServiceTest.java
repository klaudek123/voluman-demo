package com.example.demo.Volunteer.User;

import com.example.demo.Auth.AuthDto;
import com.example.demo.Config.AppException;
import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VolunteerService volunteerService;

    private Candidate candidate;
    private User user;
    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setEmail("test@example.com");
        candidate.setPhone("123456789");

        volunteer = new Volunteer();

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setVolunteer(volunteer);
    }

    // register

    @Test
    void testRegister_Success() {
        // Given
        when(volunteerService.addVolunteerFromCandidate(any(Optional.class))).thenReturn(volunteer);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        assertDoesNotThrow(() -> userService.register(Optional.of(candidate)));

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_InvalidCandidate_ThrowsException() {
        // When & Then
        assertDoesNotThrow(() -> userService.register(Optional.empty()));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_VolunteerServiceReturnsNull_ThrowsResponseStatusException() {
        // Given
        when(volunteerService.addVolunteerFromCandidate(any(Optional.class))).thenReturn(null);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.register(Optional.of(candidate)));
    }

    // generatePassword

    @Test
    void testGeneratePassword_Success() {
        // Given
        String phone = "123456789";

        // When
        String password = userService.generatePassword(phone);

        // Then
        assertNotNull(password);
        assertEquals(12, password.length());
    }

    // authenticateLogin

    @Test
    void testAuthenticateLogin_Success() {
        // Given
        when(userRepository.existsByEmailAndPassword("test@example.com", "hashedPassword")).thenReturn(true);

        // When
        boolean result = userService.authenticateLogin("test@example.com", "hashedPassword");

        // Then
        assertTrue(result);
    }

    @Test
    void testAuthenticateLogin_Failure() {
        // Given
        when(userRepository.existsByEmailAndPassword("test@example.com", "wrongPassword")).thenReturn(false);

        // When
        boolean result = userService.authenticateLogin("test@example.com", "wrongPassword");

        // Then
        assertFalse(result);
    }

    // findByUserId

    @Test
    void testFindByUserId_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        AuthDto authDto = userService.findByUserId("1");

        // Then
        assertNotNull(authDto);
        assertEquals(user.getUserId(), authDto.idUser());
    }

    @Test
    void testFindByUserId_UserNotFound_ThrowsAppException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> userService.findByUserId("1"));
        assertEquals("Unknown user", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
