package com.example.demo.Volunteer.Role;

import lombok.Getter;


@Getter
public enum VolunteerRole {
    CANDIDATE,
    VOLUNTEER,
    LEADER,
    RECRUITER,
    ADMIN
//    CANDIDATE("Candidate"),
//    VOLUNTEER("Volunteer"),
//    LEADER("Leader"),
//    RECRUITER("Recruiter"),
//    ADMIN("Admin");

//    private final String displayName;

//    VolunteerRole(String displayName) {
//        this.displayName = displayName;
//    }

//    public static VolunteerRole fromString(String name) {
//        for (VolunteerRole volunteerRole : VolunteerRole.values()) {
//            if (volunteerRole.name().equalsIgnoreCase(name) || volunteerRole.getDisplayName().equalsIgnoreCase(name)) {
//                return volunteerRole;
//            }
//        }
//        throw new IllegalArgumentException("No role found with name: " + name);
//    }
}
