package com.example.demo.Action;

import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerRepository;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Action.ActionDto.AddActionRequest;
import com.example.demo.Action.ActionDto.ChangeDescriptionRequest;
import com.example.demo.Action.ActionDto.CloseActionRequest;
import com.example.demo.Action.ActionDto.DescriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

public class ActionControllerTest {

    @InjectMocks
    private ActionController actionController;

    @Mock
    private ActionService actionService;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //TODO DODAĆ TESTY DO HEADING

    @Test
    public void testGetActions_ReturnsOk_WhenActionsListExists() {
        List<Action> actions = List.of(new Action());
        when(actionService.getAllActions()).thenReturn(actions);

        ResponseEntity<List<Action>> response = actionController.getActions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(actions, response.getBody());
    }

    @Test
    public void testGetAction_ReturnsOk_WhenActionExists() {
        Action action = new Action();
        when(actionService.getActionById(1L)).thenReturn(Optional.of(action));

        ResponseEntity<Action> response = actionController.getAction(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(action, response.getBody());
    }

    @Test
    public void testGetAction_ReturnsNotFound_WhenActionNotExist() {
        when(actionService.getActionById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Action> response = actionController.getAction(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetActionDesc_ReturnsOk_WhenActionDescExists() {
        String description = "Description";
        when(actionService.getActionDescription(1L)).thenReturn(Optional.of(description));

        ResponseEntity<DescriptionResponse> response = actionController.getActionDesc(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(description, Objects.requireNonNull(response.getBody()).description());
    }

    @Test
    public void testGetActionDesc_ReturnsNotFound_WhenActionDescNotExist() {
        when(actionService.getActionDescription(1L)).thenReturn(Optional.empty());

        ResponseEntity<DescriptionResponse> response = actionController.getActionDesc(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

//    @Test
//    public void testAddAction_ReturnsCreated_WhenSuccess() {
//        AddActionRequest request = new AddActionRequest(
//                1L,
//                "Heading",
//                "Description",
//                ActionStatus.OPEN,
//                LocalDate.now(),
//                LocalDate.now().plusDays(1),
//                2L
//        );
//        Action action = new Action();
//        when(actionService.getLeader(2L)).thenReturn(Optional.of(new Volunteer()));
//        when(actionService.createAndAddAction(request)).thenReturn(action);
//
//        ResponseEntity<?> response = actionController.addAction(request);
//
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        assertEquals(action, response.getBody());
//    }

//    @Test
//    public void testAddAction_ReturnsForbidden_WhenLeaderNotFound() {
//        AddActionRequest request = new AddActionRequest(
//                1L,
//                "Heading",
//                "Description",
//                ActionStatus.OPEN,
//                LocalDate.now(),
//                LocalDate.now().plusDays(1),
//                2L
//        );
//        when(actionService.getLeader(2L)).thenReturn(Optional.empty());
//
//        ResponseEntity<?> response = actionController.addAction(request);
//
//        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//    }

//    @Test
//    public void testAddAction_ReturnsBadRequest_WhenExceptionThrown() {
//        AddActionRequest request = new AddActionRequest(
//                1L,
//                "Heading",
//                "Description",
//                ActionStatus.OPEN,
//                LocalDate.now(),
//                LocalDate.now().plusDays(1),
//                2L
//        );
//        when(actionService.getLeader(2L)).thenReturn(Optional.of(new Volunteer()));
//        when(actionService.createAndAddAction(request)).thenThrow(new RuntimeException("Some error message"));
//
//        ResponseEntity<?> response = actionController.addAction(request);
//
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertEquals("Some error message", response.getBody());
//    }

//    @Test
//    public void testChangeDescription_ReturnsOk_WhenSuccess() {
//        ChangeDescriptionRequest request = new ChangeDescriptionRequest(1L, "New Description");
//        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(true);
//        when(actionRepository.existsById(1L)).thenReturn(true);
//
//        ResponseEntity<?> response = actionController.changeDescription(1L, request);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        verify(actionService).changeDescription(1L, "New Description");
//    }

    @Test
    public void testChangeDescription_ReturnsForbidden_WhenLeaderNotFound() {
        ChangeDescriptionRequest request = new ChangeDescriptionRequest(1L, "New Description");
        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(false);

        ResponseEntity<?> response = actionController.changeDescription(1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testChangeDescription_ReturnsNotFound_WhenActionNotFound() {
        ChangeDescriptionRequest request = new ChangeDescriptionRequest(1L, "New Description");
        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(true);
        when(actionRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<?> response = actionController.changeDescription(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

//    @Test
//    public void testCloseAction_ReturnsOk_WhenSuccess() {
//        CloseActionRequest request = new CloseActionRequest(1L);
//        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.ADMIN)).thenReturn(true);
//        when(actionRepository.existsById(1L)).thenReturn(true);
//
//        ResponseEntity<?> response = actionController.closeAction(1L, request);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        verify(actionService).closeAction(1L, 1L);
//    }

    @Test
    public void testCloseAction_ReturnsForbidden_WhenAdminNotFound() {
        CloseActionRequest request = new CloseActionRequest(1L);
        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.ADMIN)).thenReturn(false);

        ResponseEntity<?> response = actionController.closeAction(1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCloseAction_ReturnsNotFound_WhenActionNotFound() {
        CloseActionRequest request = new CloseActionRequest(1L);
        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.ADMIN)).thenReturn(true);
        when(actionRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<?> response = actionController.closeAction(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
