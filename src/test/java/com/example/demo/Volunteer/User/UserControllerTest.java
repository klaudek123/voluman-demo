package com.example.demo.Volunteer.User;

import com.example.demo.Auth.AuthDto;
import com.example.demo.Auth.AuthenticationService;
import com.example.demo.Auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WithMockUser(username = "testUser", roles = {"USER"})
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setPassword("securePassword123");
    }

    @Test
    void testGetUsers_Success() throws Exception {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testLogIn_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "securePassword123");
        AuthDto authDto = new AuthDto(1L, "sampleToken123");

        when(authenticationService.authenticate("test@example.com", "securePassword123")).thenReturn(true);
        when(userRepository.findUserIdByEmailAndPassword("test@example.com", "securePassword123")).thenReturn(1L);
        when(authenticationService.createToken(1L)).thenReturn("sampleToken123");

        // When & Then
        mockMvc.perform(post("/users/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(1L))
                .andExpect(jsonPath("$.token").value("sampleToken123"));
    }

    @Test
    void testLogIn_Failure() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongPassword");

        when(authenticationService.authenticate("test@example.com", "wrongPassword")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/users/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Błąd logowania. Sprawdź email i hasło."));
    }
}
