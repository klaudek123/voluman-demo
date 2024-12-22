package com.example.demo.Volunteer.Role;

import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

@Getter
public class RoleTransitionTable {

    private final Map<VolunteerRole, Map<VolunteerRole, Boolean>> transitions;

    public RoleTransitionTable() {
        transitions = new EnumMap<>(VolunteerRole.class);

        for (VolunteerRole fromVolunteerRole : VolunteerRole.values()) {
            transitions.put(fromVolunteerRole, new EnumMap<>(VolunteerRole.class));
            for (VolunteerRole toVolunteerRole : VolunteerRole.values()) {
                transitions.get(fromVolunteerRole).put(toVolunteerRole, null);
            }
        }
    }

    public void setTransition(VolunteerRole fromVolunteerRole, VolunteerRole toVolunteerRole, boolean canTransition) {
        if (transitions.containsKey(fromVolunteerRole) && transitions.get(fromVolunteerRole).containsKey(toVolunteerRole)) {
            transitions.get(fromVolunteerRole).put(toVolunteerRole, canTransition);
        } else {
            throw new IllegalArgumentException("Invalid roles specified.");
        }
    }

    public Boolean canTransition(VolunteerRole fromVolunteerRole, VolunteerRole toVolunteerRole) {
        if (transitions.containsKey(fromVolunteerRole) && transitions.get(fromVolunteerRole).containsKey(toVolunteerRole)) {
            return transitions.get(fromVolunteerRole).get(toVolunteerRole);
        } else {
            throw new IllegalArgumentException("Invalid roles specified.");
        }
    }

    public void validateTable() {
        for (Map.Entry<VolunteerRole, Map<VolunteerRole, Boolean>> fromEntry : transitions.entrySet()) {
            for (Map.Entry<VolunteerRole, Boolean> toEntry : fromEntry.getValue().entrySet()) {
                VolunteerRole fromVolunteerRole = fromEntry.getKey();
                VolunteerRole toVolunteerRole = toEntry.getKey();

                if (toEntry.getValue() == null) {
                    throw new IllegalStateException(
                            "Undefined transition: " + fromVolunteerRole + " → " + toVolunteerRole
                    );
                }
            }
        }
    }


}
