package com.example.demo.Volunteer.Role;

import lombok.Getter;

@Getter
public class RoleException extends RuntimeException {
    private final VolunteerRole fromVolunteerRole;
    private final VolunteerRole toVolunteerRole;

    public RoleException(String message, VolunteerRole fromVolunteerRole, VolunteerRole toVolunteerRole) {
        super(message);
        this.fromVolunteerRole = fromVolunteerRole;
        this.toVolunteerRole = toVolunteerRole;
    }

}
