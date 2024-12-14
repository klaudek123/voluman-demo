package com.example.demo.Volunteer.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTransitionTableTest {

    private RoleTransitionTable transitionTable;

    @BeforeEach
    void setUp() {
        transitionTable = new RoleTransitionTable();
    }

    // setTransition

    @Test
    void testSetTransition_Success() {
        // Given
        VolunteerRole fromRole = VolunteerRole.CANDIDATE;
        VolunteerRole toRole = VolunteerRole.VOLUNTEER;

        // When
        transitionTable.setTransition(fromRole, toRole, true);

        // Then
        assertTrue(transitionTable.canTransition(fromRole, toRole));
    }

    @Test
    void testSetTransition_InvalidRoles() {
        // Given
        VolunteerRole fromRole = null;
        VolunteerRole toRole = VolunteerRole.VOLUNTEER;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> transitionTable.setTransition(fromRole, toRole, true));
    }

    // canTransition

    @Test
    void testCanTransition_ReturnsTrue() {
        // Given
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.VOLUNTEER, true);

        // When
        Boolean result = transitionTable.canTransition(VolunteerRole.CANDIDATE, VolunteerRole.VOLUNTEER);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanTransition_ReturnsFalse() {
        // Given
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.LEADER, false);

        // When
        Boolean result = transitionTable.canTransition(VolunteerRole.CANDIDATE, VolunteerRole.LEADER);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanTransition_ReturnsNullForUndefinedTransition() {
        // Given
        VolunteerRole fromRole = VolunteerRole.CANDIDATE;
        VolunteerRole toRole = VolunteerRole.ADMIN;

        // When
        Boolean result = transitionTable.canTransition(fromRole, toRole);

        // Then
        assertNull(result);
    }

    @Test
    void testCanTransition_InvalidRoles() {
        // Given
        VolunteerRole fromRole = null;
        VolunteerRole toRole = VolunteerRole.VOLUNTEER;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> transitionTable.canTransition(fromRole, toRole));
    }

    // validateTable

    @Test
    void testValidateTable_Success() {
        // Given
        for (VolunteerRole fromRole : VolunteerRole.values()) {
            for (VolunteerRole toRole : VolunteerRole.values()) {
                transitionTable.setTransition(fromRole, toRole, false);
            }
        }

        // When & Then
        assertDoesNotThrow(() -> transitionTable.validateTable());
    }

    @Test
    void testValidateTable_ThrowsExceptionForUndefinedTransition() {
        // Given
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.VOLUNTEER, true);
        // Other transitions are undefined

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transitionTable.validateTable());
        assertTrue(exception.getMessage().contains("Undefined transition"));
    }
}
