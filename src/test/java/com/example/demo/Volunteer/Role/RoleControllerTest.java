package com.example.demo.Volunteer.Role;

import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerDetails;
import com.example.demo.Volunteer.VolunteerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "testUser", roles = {"USER"})
@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private VolunteerService volunteerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        volunteer = new Volunteer();
        volunteer.setVolunteerId(1L);
        volunteer.setRole(VolunteerRole.CANDIDATE);

        VolunteerDetails details = new VolunteerDetails();
        details.setFirstname("John");
        details.setLastname("Doe");
        details.setEmail("test@example.com");
        details.setPhone("123-456-789");
        details.setDateOfBirth(LocalDate.of(1990, 1, 1));
        details.setStreet("Main Street");
        details.setCity("Sample City");
        details.setHouseNumber("123");
        details.setApartmentNumber("4A");
        details.setPostalNumber("12-345");
        details.setSex("M");

        volunteer.setVolunteerDetails(details);
    }


    // GET /api/roles

    @Test
    void testGetRoles_ReturnsAllRoles() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/roles")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(VolunteerRole.values().length))
                .andExpect(jsonPath("$[0]").value(VolunteerRole.CANDIDATE.name()));
    }

    // GET /api/roles/can-transition

    @Test
    void testCanTransition_ReturnsTrue() throws Exception {
        // Given
        when(roleService.canTransition(VolunteerRole.CANDIDATE, VolunteerRole.VOLUNTEER)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/roles/can-transition")
                        .param("fromRole", "CANDIDATE")
                        .param("toRole", "VOLUNTEER"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCanTransition_ReturnsFalse() throws Exception {
        // Given
        when(roleService.canTransition(VolunteerRole.CANDIDATE, VolunteerRole.LEADER)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/roles/can-transition")
                        .param("fromRole", "CANDIDATE")
                        .param("toRole", "LEADER"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // POST /api/roles/assign

    @Test
    void testAssignRole_Success() throws Exception {
        // Given
        when(volunteerService.findVolunteerById(1L)).thenReturn(volunteer);
        doNothing().when(roleService).assignRole(any(Volunteer.class), eq(VolunteerRole.VOLUNTEER));

        // When & Then
        mockMvc.perform(post("/api/roles/assign")
                        .with(csrf())
                        .param("userId", "1")
                        .param("newVolunteerRole", "VOLUNTEER"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role assigned successfully: test@example.com is now VOLUNTEER"));

        verify(roleService, times(1)).assignRole(any(Volunteer.class), eq(VolunteerRole.VOLUNTEER));
    }

    @Test
    void testAssignRole_ThrowsRoleException() throws Exception {
        // Given
        when(volunteerService.findVolunteerById(1L)).thenReturn(volunteer);
        doThrow(new RoleException("Volunteer already has the role: VOLUNTEER", VolunteerRole.VOLUNTEER, VolunteerRole.VOLUNTEER))
                .when(roleService).assignRole(any(Volunteer.class), eq(VolunteerRole.VOLUNTEER));

        // When & Then
        mockMvc.perform(post("/api/roles/assign")
                        .with(csrf())
                        .param("userId", "1")
                        .param("newVolunteerRole", "VOLUNTEER"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The user is already in the role: VOLUNTEER"));
    }

    @Test
    void testAssignRole_InvalidRole() throws Exception {
        mockMvc.perform(post("/api/roles/assign")
                        .with(csrf())
                        .param("userId", "1")
                        .param("newVolunteerRole", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }

    // RoleException handler

    @Test
    void testHandleRoleException() throws Exception {
        // Given
        when(volunteerService.findVolunteerById(1L)).thenReturn(volunteer);
        doThrow(new RoleException("Cannot transition from CANDIDATE to LEADER", VolunteerRole.CANDIDATE, VolunteerRole.LEADER))
                .when(roleService).assignRole(any(Volunteer.class), eq(VolunteerRole.LEADER));

        // When & Then
        mockMvc.perform(post("/api/roles/assign")
                        .with(csrf())
                        .param("userId", "1")
                        .param("newVolunteerRole", "LEADER"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot transition from CANDIDATE to LEADER"));
    }
}
