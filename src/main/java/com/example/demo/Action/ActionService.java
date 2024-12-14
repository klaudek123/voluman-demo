package com.example.demo.Action;

import com.example.demo.Volunteer.*;
import com.example.demo.Action.ActionDto.AddActionRequest;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Volunteer.VolunteerDto.LeaderDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ActionService {
    private final ActionRepository actionRepository;
    private final VolunteerRepository volunteerRepository;


    public ActionService(ActionRepository actionRepository, VolunteerRepository volunteerRepository) {
        this.actionRepository = actionRepository;
        this.volunteerRepository = volunteerRepository;
    }

    public List<Action> getAllActions() {
        return actionRepository.findAll();
    }

    public Optional<Action> getActionById(Long idAction) {
        return actionRepository.findById(idAction);
    }

    public Optional<String> getActionDescription(Long idAction) {
        return actionRepository.findById(idAction).map(Action::getDescription);
    }

    public Optional<String> getActionHeading(Long idAction) {
        return actionRepository.findById(idAction).map(Action::getHeading);
    }

    public Action addAction(Action action) {
        return actionRepository.save(action);
    }

    public Action createAndAddAction(AddActionRequest request) {
        Action action = new Action();
        action.setHeading(request.heading());
        action.setDescription(request.description());
        action.setStatus(request.status());
        action.setStartDay(request.startDay());
        action.setEndDay(request.endDay());
        action.setLeader(getLeaderDto(request.leaderId()).get());
        action.setDemands(new ArrayList<>());

        return addAction(action);
    }


    @Transactional
    public void closeAction(Long idAction, Long adminId) {
        Optional<Action> optionalAction = getActionById(idAction);

        if (optionalAction.isPresent()) {
            Action action = optionalAction.get();
            action.setStatus(ActionStatus.CLOSED);
            actionRepository.save(action);
        }
        }

    @Transactional
    public void changeDescription(Long idAction, String description) { //DONE

        Optional<Action> optionalAction = getActionById(idAction);

        if (optionalAction.isPresent()) {
            Action action = optionalAction.get();
            action.setDescription(description);
            actionRepository.save(action);
        }

    }

    public Optional<Volunteer> getLeader(Long leaderId) {
        return volunteerRepository.findById(leaderId);
    }

    public Optional<LeaderDto> getLeaderDto(Long leaderId) {
        return volunteerRepository.findById(leaderId)
                .filter(volunteer -> volunteer.getRole() == VolunteerRole.LEADER)
                .map(volunteer -> {
                    VolunteerDetails details = volunteer.getVolunteerDetails();
                    return new LeaderDto(
                            volunteer.getVolunteerId(),
                            details.getFirstname(),
                            details.getLastname(),
                            details.getEmail(),
                            details.getPhone()
                    );
                });
    }

    public void addDetermined(Long actionId, Long volunteerId) {
        Optional<Action> actionOptional = actionRepository.findById(actionId);
        if (actionOptional.isPresent()) {
            Optional<Volunteer> volunteerOptional = volunteerRepository.findById(volunteerId);

            volunteerOptional.ifPresent(volunteer -> actionOptional.get().getDetermined().add(volunteer));
        }
    }

    public void addVolunteer(Long actionId, Long volunteerId) {
        Optional<Action> actionOptional = actionRepository.findById(actionId);
        if (actionOptional.isPresent()) {
            Optional<Volunteer> volunteerOptional = volunteerRepository.findById(volunteerId);

            volunteerOptional.ifPresent(volunteer -> actionOptional.get().getVolunteers().add(volunteer));
        }
    }
}

