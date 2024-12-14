package com.example.demo.Action;

import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerDto.LeaderDto;
import com.example.demo.Volunteer.VolunteerDetails;
import com.example.demo.Volunteer.VolunteerRepository;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Action.ActionDto.AddActionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ActionServiceTest {

    @InjectMocks
    private ActionService actionService;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllActions() {
        List<Action> actions = List.of(new Action());
        when(actionRepository.findAll()).thenReturn(actions);

        List<Action> result = actionService.getAllActions();

        assertEquals(actions, result);
    }

    @Test
    public void testGetActionById() {
        Action action = new Action();
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        Optional<Action> result = actionService.getActionById(1L);

        assertTrue(result.isPresent());
        assertEquals(action, result.get());
    }

    @Test
    public void testGetActionById_NotFound() {
        when(actionRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Action> result = actionService.getActionById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetActionDescription() {
        Action action = new Action();
        action.setDescription("Description");
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        Optional<String> result = actionService.getActionDescription(1L);

        assertTrue(result.isPresent());
        assertEquals("Description", result.get());
    }

    @Test
    public void testGetActionDescription_NotFound() {
        when(actionRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<String> result = actionService.getActionDescription(1L);

        assertTrue(result.isEmpty());
    }

    //TODO Poprawić
    @Test
    public void testGetActionHeading() {
        Action action = new Action();
        action.setHeading("Heading");
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        Optional<String> result = actionService.getActionHeading(1L);

        assertTrue(result.isPresent());
        assertEquals("Heading", result.get());
    }

    @Test
    public void testGetActionHeading_NotFound() {
        when(actionRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<String> result = actionService.getActionHeading(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testAddAction() {
        Action action = new Action();
        when(actionRepository.save(action)).thenReturn(action);

        Action result = actionService.addAction(action);

        assertEquals(action, result);
    }

    @Test
    public void testCreateAndAddAction() {
        AddActionRequest request = new AddActionRequest(
                1L,
                "Heading",
                "Description",
                ActionStatus.OPEN,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                2L
        );
        Volunteer leader = new Volunteer();
        leader.setRole(VolunteerRole.LEADER);
        VolunteerDetails details = new VolunteerDetails();
        details.setFirstname("John");
        details.setLastname("Doe");
        details.setEmail("john.doe@example.com");
        details.setPhone("123456789");
        details.setDateOfBirth(LocalDate.now());
        details.setCity("City");
        details.setStreet("Street");
        details.setHouseNumber("1A");
        details.setApartmentNumber("10");
        details.setPostalNumber("12345");
        details.setSex("M");
        leader.setVolunteerDetails(details);

        when(volunteerRepository.findById(2L)).thenReturn(Optional.of(leader));

        Action action = new Action();
        when(actionRepository.save(any(Action.class))).thenReturn(action);

        Action result = actionService.createAndAddAction(request);

        assertEquals(action, result);
    }

    @Test
    public void testCloseAction() {
        Action action = new Action();
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        actionService.closeAction(1L, 1L);

        assertEquals(ActionStatus.CLOSED, action.getStatus());
        verify(actionRepository).save(action);
    }

    @Test
    public void testChangeDescription() {
        Action action = new Action();
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        actionService.changeDescription(1L, "New Description");

        assertEquals("New Description", action.getDescription());
        verify(actionRepository).save(action);
    }

    @Test
    public void testGetLeader() {
        Volunteer leader = new Volunteer();
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(leader));

        Optional<Volunteer> result = actionService.getLeader(1L);

        assertTrue(result.isPresent());
        assertEquals(leader, result.get());
    }

    @Test
    public void testGetLeaderDto() {
        Volunteer volunteer = new Volunteer();
        volunteer.setRole(VolunteerRole.LEADER);
        VolunteerDetails details = new VolunteerDetails();
        details.setFirstname("Name");
        details.setLastname("Lastname");
        details.setEmail("email@example.com");
        details.setPhone("123456789");
        details.setDateOfBirth(LocalDate.now());
        details.setCity("City");
        details.setStreet("Street");
        details.setHouseNumber("1A");
        details.setApartmentNumber("10");
        details.setPostalNumber("12345");
        details.setSex("M");
        volunteer.setVolunteerDetails(details);

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

        Optional<LeaderDto> result = actionService.getLeaderDto(1L);

        assertTrue(result.isPresent());
        LeaderDto dto = result.get();
        assertEquals(volunteer.getVolunteerId(), dto.leaderId());
        assertEquals(details.getFirstname(), dto.name());
        assertEquals(details.getLastname(), dto.lastname());
        assertEquals(details.getEmail(), dto.email());
        assertEquals(details.getPhone(), dto.phone());
    }

    @Test
    public void testAddDetermined() {
        Action action = new Action();
        Volunteer volunteer = new Volunteer();
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));
        when(volunteerRepository.findById(2L)).thenReturn(Optional.of(volunteer));

        actionService.addDetermined(1L, 2L);

        assertTrue(action.getDetermined().contains(volunteer));
    }

    @Test
    public void testAddVolunteer() {
        Action action = new Action();
        Volunteer volunteer = new Volunteer();
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));
        when(volunteerRepository.findById(2L)).thenReturn(Optional.of(volunteer));

        actionService.addVolunteer(1L, 2L);

        assertTrue(action.getVolunteers().contains(volunteer));
    }
}