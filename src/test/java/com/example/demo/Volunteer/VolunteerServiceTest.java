package com.example.demo.Volunteer;

import com.example.demo.Action.Action;
import com.example.demo.Action.ActionService;
import com.example.demo.Schedule.Decision;
import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Preferences.Preferences;
import com.example.demo.Volunteer.Preferences.PreferencesService;
import com.example.demo.Volunteer.Role.RoleService;
import com.example.demo.Volunteer.Role.VolunteerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private ActionService actionService;

    @Mock
    private PreferencesService preferencesService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private VolunteerService volunteerService;

    private Candidate candidate;
    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        candidate = new Candidate();
        candidate.setFirstname("John");
        candidate.setLastname("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setPhone("123456789");
        candidate.setDateOfBirth(LocalDate.of(1990, 1, 1));
        candidate.setCity("City");
        candidate.setStreet("Street");
        candidate.setHouseNumber("1");
        candidate.setPostalNumber("12345");
        candidate.setSex("M");

        volunteer = new Volunteer();
        volunteer.setVolunteerId(1L);
        volunteer.setRole(VolunteerRole.CANDIDATE);
        volunteer.setVolunteerDetails(new VolunteerDetails());
    }

    // addVolunteerFromCandidate

//    @Test
//    void testAddVolunteerFromCandidate_Success() {
//        // Given
//        when(volunteerRepository.save(any(Volunteer.class))).thenReturn(volunteer);
//
//        // When
//        Volunteer result = volunteerService.addVolunteerFromCandidate(Optional.of(candidate));
//
//        // Then
//        assertNotNull(result);
//        assertEquals(VolunteerRole.VOLUNTEER, result.getRole());
//        verify(roleService, times(1)).assignRole(any(Volunteer.class), eq(VolunteerRole.VOLUNTEER));
//        verify(volunteerRepository, times(1)).save(any(Volunteer.class));
//    }

    @Test
    void testAddVolunteerFromCandidate_NullCandidate() {
        // When
        Volunteer result = volunteerService.addVolunteerFromCandidate(Optional.empty());

        // Then
        assertNull(result);
        verify(roleService, never()).assignRole(any(Volunteer.class), any(VolunteerRole.class));
        verify(volunteerRepository, never()).save(any(Volunteer.class));
    }

    // addVolunteer

    @Test
    void testAddVolunteer_Success() {
        // Given
        when(volunteerRepository.save(any(Volunteer.class))).thenReturn(volunteer);

        // When
        Volunteer result = volunteerService.addVolunteer(volunteer);

        // Then
        assertNotNull(result);
        verify(roleService, times(1)).assignRole(volunteer, VolunteerRole.VOLUNTEER);
        verify(volunteerRepository, times(1)).save(volunteer);
    }

    // addPreferences

    @Test
    void testAddPreferences_Success() {
        // Given
        Long actionId = 1L;
        Long volunteerId = 1L;
        Decision decision = Decision.T;

        Action action = new Action();
        action.setActionId(actionId);

        Preferences preferences = new Preferences();

        volunteer.setPreferences(preferences);

        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(volunteer));
        when(actionService.getActionById(actionId)).thenReturn(Optional.of(action));

        // When
        volunteerService.addPreferences(actionId, volunteerId, decision);

        // Then
        verify(preferencesService, times(1)).addPreferences(preferences);
        verify(volunteerRepository, times(1)).save(volunteer);
    }

    @Test
    void testAddPreferences_VolunteerNotFound() {
        // Given
        Long actionId = 1L;
        Long volunteerId = 1L;
        Decision decision = Decision.T;

        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.empty());

        // When
        volunteerService.addPreferences(actionId, volunteerId, decision);

        // Then
        verify(preferencesService, never()).addPreferences(any(Preferences.class));
        verify(volunteerRepository, never()).save(any(Volunteer.class));
    }

    // isLeaderExist

    @Test
    void testIsLeaderExist_ReturnsTrue() {
        // Given
        when(volunteerRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = volunteerService.isLeaderExist(1L);

        // Then
        assertTrue(result);
        verify(volunteerRepository, times(1)).existsById(1L);
    }

    @Test
    void testIsLeaderExist_ReturnsFalse() {
        // Given
        when(volunteerRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = volunteerService.isLeaderExist(1L);

        // Then
        assertFalse(result);
        verify(volunteerRepository, times(1)).existsById(1L);
    }

    // findVolunteerById

    @Test
    void testFindVolunteerById_Success() {
        // Given
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

        // When
        Volunteer result = volunteerService.findVolunteerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getVolunteerId());
        verify(volunteerRepository, times(1)).findById(1L);
    }

    @Test
    void testFindVolunteerById_NotFound() {
        // Given
        when(volunteerRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThrows(java.util.NoSuchElementException.class, () -> {
            volunteerService.findVolunteerById(1L);
        });
    }
}
