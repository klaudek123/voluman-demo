package com.example.demo.Volunteer.Role;

import com.example.demo.Volunteer.Volunteer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTest {

    private RoleService roleService;
    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        roleService = new RoleService();
        volunteer = new Volunteer();
    }

    @Test
    void testAssignRole_SuccessfulTransition() {
        // Given
        volunteer.setRole(VolunteerRole.CANDIDATE);

        // When
        roleService.assignRole(volunteer, VolunteerRole.VOLUNTEER);

        // Then
        assertEquals(VolunteerRole.VOLUNTEER, volunteer.getRole());
    }

    @Test
    void testAssignRole_FailedTransition() {
        // Given
        volunteer.setRole(VolunteerRole.CANDIDATE);

        // When & Then
        RoleException exception = assertThrows(RoleException.class, () ->
                roleService.assignRole(volunteer, VolunteerRole.LEADER));

        assertEquals("Cannot transition from CANDIDATE to LEADER", exception.getMessage());
    }

    @Test
    void testAssignRole_SameRoleException() {
        // Given
        volunteer.setRole(VolunteerRole.RECRUITER);

        // When & Then
        RoleException exception = assertThrows(RoleException.class, () ->
                roleService.assignRole(volunteer, VolunteerRole.RECRUITER));

        assertEquals("Volunteer already has the role: RECRUITER", exception.getMessage());
    }

    @Test
    void testAssignRole_UndefinedTransition() {
        // Given
        volunteer.setRole(VolunteerRole.ADMIN);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                roleService.assignRole(volunteer, null));

        assertEquals("Invalid roles specified.", exception.getMessage());
    }

    @Test
    void testCanTransition_ReturnsTrueForValidTransition() {
        // Given
        VolunteerRole fromRole = VolunteerRole.CANDIDATE;
        VolunteerRole toRole = VolunteerRole.VOLUNTEER;

        // When
        Boolean result = roleService.canTransition(fromRole, toRole);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanTransition_ReturnsFalseForInvalidTransition() {
        // Given
        VolunteerRole fromRole = VolunteerRole.CANDIDATE;
        VolunteerRole toRole = VolunteerRole.LEADER;

        // When
        Boolean result = roleService.canTransition(fromRole, toRole);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanTransition_ReturnsNullForUndefinedTransition() {
        // Given
        VolunteerRole fromRole = VolunteerRole.ADMIN;
        VolunteerRole toRole = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                roleService.canTransition(fromRole, toRole));

        assertEquals("Invalid roles specified.", exception.getMessage());
    }
}
