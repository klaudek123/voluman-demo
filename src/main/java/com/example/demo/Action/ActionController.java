package com.example.demo.Action;

import com.example.demo.Action.ActionDto.*;
import com.example.demo.Log.EventType;
import com.example.demo.Log.LogService;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Volunteer.VolunteerRepository;
import com.example.demo.Volunteer.VolunteerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/actions")
public class ActionController {
    private final ActionService actionService;
    private final ActionRepository actionRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerService volunteerService;
    private final LogService logService;

    public ActionController(ActionService actionService, ActionRepository actionRepository, VolunteerRepository volunteerRepository, VolunteerService volunteerService, LogService logService) {
        this.actionService = actionService;
        this.actionRepository = actionRepository;
        this.volunteerRepository = volunteerRepository;
        this.volunteerService = volunteerService;
        this.logService = logService;
    }

    @GetMapping("")
    public ResponseEntity<List<Action>> getActions() { //DONE
        return ResponseEntity.ok(actionService.getAllActions());
    }

    @GetMapping("/{idAction}")
    public ResponseEntity<Action> getAction(@PathVariable("idAction") Long idAction) { //DONE
        return actionService.getActionById(idAction)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{idAction}/description")
    public ResponseEntity<DescriptionResponse> getActionDesc(@PathVariable("idAction") Long idAction) { //DONE
        return actionService.getActionDescription(idAction).map(DescriptionResponse::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{idAction}/heading")
    public ResponseEntity<HeadingResponse> getActionHeading(@PathVariable("idAction") Long idAction) { //DONE
        return actionService.getActionHeading(idAction).map(HeadingResponse::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("")
    public ResponseEntity<?> addAction(@RequestBody AddActionRequest request) { //DONE
        if (!volunteerService.isLeaderExist(request.leaderId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } //TODO nie dziala
        try {
            Action newAction = actionService.createAndAddAction(request);

            logService.logAction(request.adminId(), EventType.ADD, "Admin added action with id:" + newAction.getActionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(newAction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Tmp metoda
    @DeleteMapping("/{actionId}")
    public ResponseEntity<?> deleteAction(@PathVariable Long actionId) {
        actionRepository.deleteById(actionId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{idAction}/description")
    public ResponseEntity<?> changeDescription(@PathVariable("idAction") Long idAction, @RequestBody ChangeDescriptionRequest request) { //DONE
        if (!volunteerRepository.existsByVolunteerIdAndRole(request.leaderId(), VolunteerRole.LEADER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!actionRepository.existsById(idAction)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            actionService.changeDescription(idAction, request.description());

            logService.logAction(request.leaderId(), EventType.UPDATE, "Leader updated description of action with id:" + request.description());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{idAction}/close")
    public ResponseEntity<?> closeAction(@PathVariable("idAction") Long idAction, @RequestBody CloseActionRequest request) { //DONE
        if (!volunteerRepository.existsByVolunteerIdAndRole(request.adminId(), VolunteerRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!actionRepository.existsById(idAction)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            actionService.closeAction(idAction, request.adminId());

            logService.logAction(request.adminId(), EventType.UPDATE, "Admin closed action with id:" + request.adminId());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
