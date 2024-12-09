package com.example.demo.Volunteer.Role;

import com.example.demo.Volunteer.Volunteer;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class RoleService {

    private final RoleTransitionTable transitionTable;

    public RoleService() {
        this.transitionTable = new RoleTransitionTable();

        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.CANDIDATE, false);
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.VOLUNTEER, true);
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.LEADER, false);
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.RECRUITER, false);
        transitionTable.setTransition(VolunteerRole.CANDIDATE, VolunteerRole.ADMIN, false);

        transitionTable.setTransition(VolunteerRole.VOLUNTEER, VolunteerRole.CANDIDATE, false);
        transitionTable.setTransition(VolunteerRole.VOLUNTEER, VolunteerRole.VOLUNTEER, false);
        transitionTable.setTransition(VolunteerRole.VOLUNTEER, VolunteerRole.LEADER, true);
        transitionTable.setTransition(VolunteerRole.VOLUNTEER, VolunteerRole.RECRUITER, true);
        transitionTable.setTransition(VolunteerRole.VOLUNTEER, VolunteerRole.ADMIN, true);

        transitionTable.setTransition(VolunteerRole.LEADER, VolunteerRole.CANDIDATE, false);
        transitionTable.setTransition(VolunteerRole.LEADER, VolunteerRole.VOLUNTEER, true);
        transitionTable.setTransition(VolunteerRole.LEADER, VolunteerRole.LEADER, false);
        transitionTable.setTransition(VolunteerRole.LEADER, VolunteerRole.RECRUITER, true);
        transitionTable.setTransition(VolunteerRole.LEADER, VolunteerRole.ADMIN, true);

        transitionTable.setTransition(VolunteerRole.RECRUITER, VolunteerRole.CANDIDATE, false);
        transitionTable.setTransition(VolunteerRole.RECRUITER, VolunteerRole.VOLUNTEER, true);
        transitionTable.setTransition(VolunteerRole.RECRUITER, VolunteerRole.LEADER, true);
        transitionTable.setTransition(VolunteerRole.RECRUITER, VolunteerRole.RECRUITER, false);
        transitionTable.setTransition(VolunteerRole.RECRUITER, VolunteerRole.ADMIN, true);

        transitionTable.setTransition(VolunteerRole.ADMIN, VolunteerRole.CANDIDATE, false);
        transitionTable.setTransition(VolunteerRole.ADMIN, VolunteerRole.VOLUNTEER, true);
        transitionTable.setTransition(VolunteerRole.ADMIN, VolunteerRole.LEADER, true);
        transitionTable.setTransition(VolunteerRole.ADMIN, VolunteerRole.RECRUITER, true);
        transitionTable.setTransition(VolunteerRole.ADMIN, VolunteerRole.ADMIN, false);

        validateTransitions();
    }

    public void assignRole(Volunteer volunteer, VolunteerRole newVolunteerRole) {
        VolunteerRole currentVolunteerRole = volunteer.getRole();

        if (currentVolunteerRole == newVolunteerRole) {
            throw new RoleException("Volunteer already has the role: " + newVolunteerRole.toString(), currentVolunteerRole, newVolunteerRole);
        }

        Boolean canTransition = transitionTable.canTransition(currentVolunteerRole, newVolunteerRole);

        if (canTransition == null) {
            throw new RoleException("Transition undefined from " + currentVolunteerRole + " to " + newVolunteerRole, currentVolunteerRole, newVolunteerRole);
        }

        if (!canTransition) {
            throw new RoleException("Cannot transition from " + currentVolunteerRole + " to " + newVolunteerRole, currentVolunteerRole, newVolunteerRole);
        }

        volunteer.setRole(newVolunteerRole);
    }

    public Boolean canTransition(VolunteerRole fromVolunteerRole, VolunteerRole toVolunteerRole) {
        return transitionTable.canTransition(fromVolunteerRole, toVolunteerRole);
    }

    private void validateTransitions() {
        try {
            transitionTable.validateTable();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Transition table validation failed: " + e.getMessage());
        }
    }

}
