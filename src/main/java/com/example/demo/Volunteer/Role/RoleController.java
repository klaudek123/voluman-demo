package com.example.demo.Volunteer.Role;

import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final VolunteerService volunteerService;

    @Autowired
    public RoleController(RoleService roleService, VolunteerService volunteerService) {
        this.roleService = roleService;
        this.volunteerService = volunteerService;
    }

    @GetMapping
    public VolunteerRole[] getRoles() {
        return VolunteerRole.values();
    }

    @GetMapping("/can-transition")
    public ResponseEntity<Boolean> canTransition(
            @RequestParam String fromRole,
            @RequestParam String toRole) {
        VolunteerRole from = VolunteerRole.valueOf(fromRole);
        VolunteerRole to = VolunteerRole.valueOf(toRole);
        Boolean canTransition = roleService.canTransition(from, to);
        return ResponseEntity.ok(canTransition);
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignRole(
            @RequestParam Long userId,
            @RequestParam VolunteerRole newVolunteerRole) {
        Volunteer volunteer = volunteerService.findVolunteerById(userId);

        roleService.assignRole(volunteer, newVolunteerRole);
        return ResponseEntity.ok("Role assigned successfully: " + volunteer.getVolunteerDetails().getEmail() + " is now " + newVolunteerRole.toString());
    }

    @ExceptionHandler(RoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleRoleException(RoleException e) {
        String message = e.getMessage();

        if (e.getFromVolunteerRole() != null && e.getFromVolunteerRole() == e.getToVolunteerRole()) {
            message = "The user is already in the role: " + e.getFromVolunteerRole().toString();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
