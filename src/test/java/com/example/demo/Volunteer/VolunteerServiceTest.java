package com.example.demo.Volunteer;

import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Preferences.Preferences;
import com.example.demo.Volunteer.Preferences.PreferencesService;
import com.example.demo.Schedule.Decision;
import com.example.demo.Action.Action;
import com.example.demo.Action.ActionRepository;
import com.example.demo.Action.ActionService;
import com.example.demo.Volunteer.Role.VolunteerRole;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private ActionService actionService;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private PreferencesService preferencesService;

    @InjectMocks
    private VolunteerService volunteerService;

    @Test
    public void testAddVolunteerFromCandidate() {
        Candidate candidate = new Candidate();
        candidate.setFirstname("John");
        candidate.setLastname("Doe");
        Optional<Candidate> candidateOptional = Optional.of(candidate);

        volunteerService.addVolunteerFromCandidate(candidateOptional);

        verify(volunteerRepository, times(1)).save(any(Volunteer.class));
    }

    @Test
    public void testPromoteToLeader() {
        Long idVolunteer = 1L;
        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerRole(VolunteerRole.VOLUNTEER);

        when(volunteerRepository.findByVolunteerIdAndRole(idVolunteer, VolunteerRole.VOLUNTEER)).thenReturn(Optional.of(volunteer));

        volunteerService.promoteToLeader(idVolunteer);

        verify(volunteerRepository, times(1)).save(volunteer);
        assertEquals(VolunteerRole.LEADER, volunteer.getVolunteerRole());
    }

    @Test
    public void testDegradeLeader() {
        Long idVolunteer = 1L;
        Volunteer volunteer = new Volunteer();
        volunteer.setRole(VolunteerRole.LEADER);

        when(volunteerRepository.findByVolunteerIdAndRole(idVolunteer, VolunteerRole.LEADER)).thenReturn(Optional.of(volunteer));

        volunteerService.degradeLeader(idVolunteer);

        verify(volunteerRepository, times(1)).save(volunteer);
        assertEquals(VolunteerRole.VOLUNTEER, volunteer.getVolunteerRole());
    }

    @Test
    public void testAddPreferences() {
        Long actionId = 1L;
        Long volunteerId = 1L;
        Decision decision = Decision.T;
        Volunteer volunteer = new Volunteer();
        volunteer.setPreferences(new Preferences());
        Action action = new Action();

        // Ustawienie mocka dla repository i service
        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(volunteer));
        when(actionService.getActionById(actionId)).thenReturn(Optional.of(action));

        // Wywołanie testowanej metody
        volunteerService.addPreferences(actionId, volunteerId, decision);

        // Sprawdzenie, czy findById zostało wywołane raz
        verify(volunteerRepository, times(1)).findById(volunteerId);

        // Sprawdzenie, czy save zostało wywołane raz i z odpowiednim obiektem Volunteer
        verify(volunteerRepository, times(1)).save(volunteer);

        // Dodatkowe asercje, aby upewnić się, że preferencje zostały dodane poprawnie
        assertTrue(volunteer.getPreferences().getT().contains(action));
    }
}
