package com.example.demo.Volunteer;


import com.example.demo.Log.EventType;
import com.example.demo.Log.LogService;
import com.example.demo.Log.LogUserDto;
import com.example.demo.Volunteer.VolunteerDto.AdminRequest;
import com.example.demo.Volunteer.VolunteerDto.VolunteerRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/volunteers")
public class VolunteerController {
    private final VolunteerRepository volunteerRepository;
    private final VolunteerService volunteerService;
    private final LogService logService;

    public VolunteerController(VolunteerRepository volunteerRepository, VolunteerService volunteerService, LogService logService) {
        this.volunteerRepository = volunteerRepository;
        this.volunteerService = volunteerService;
        this.logService = logService;
    }

    @GetMapping("")
    public ResponseEntity<List<Volunteer>> getVolunteers() { //DONE
        return ResponseEntity.ok(volunteerRepository.findAll());
    }

    @PostMapping("")
    public ResponseEntity<Volunteer> addVolunteer(@RequestBody Volunteer volunteer) {
        return ResponseEntity.ok(volunteerService.addVolunteer(volunteer));
    }

    @DeleteMapping("/{volunteerId}/delete")
    public ResponseEntity<?> deleteVolunteer(@PathVariable Long volunteerId) {
        volunteerRepository.deleteById(volunteerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{idVolunteer}")
    public ResponseEntity<Volunteer> getVolunteer(@PathVariable Long idVolunteer) { //DONE
        Optional<Volunteer> volunteer = volunteerRepository.findById(idVolunteer);
        return volunteer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/leaders")
    public ResponseEntity<List<Volunteer>> getVolunteerLeaders() { //DONE
        List<Volunteer> leaders = volunteerRepository.findAllByRole(VolunteerRole.LEADER);
        if (leaders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(leaders);
    }

    @GetMapping("/leaders/{idVolunteer}")
    public ResponseEntity<Volunteer> getVolunteerLeader(@PathVariable Long idVolunteer) { //DONE
        Optional<Volunteer> leader = volunteerRepository.findByVolunteerIdAndRole(idVolunteer, VolunteerRole.LEADER);
        return leader.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/{idVolunteer}/promote")
    public ResponseEntity<Void> promoteToLeader(@PathVariable Long idVolunteer, @RequestBody AdminRequest request) {
        if (!volunteerRepository.existsByVolunteerIdAndRole(request.adminId(), VolunteerRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(volunteerRepository.existsByVolunteerIdAndRole(idVolunteer, VolunteerRole.LEADER)){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        }
        Optional<Volunteer> volunteer = volunteerRepository.findById(idVolunteer);


        if (volunteer.isPresent()) {
            volunteerService.promoteToLeader(idVolunteer);

            logService.logVolunteer(volunteer.get(), EventType.UPDATE, "Promoted by admin with id: " + request.adminId());

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{idVolunteer}/degrade")
    public ResponseEntity<Void> degradeLeader(@PathVariable Long idVolunteer, @RequestBody AdminRequest request) {
        if (!volunteerRepository.existsByVolunteerIdAndRole(request.adminId(), VolunteerRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(volunteerRepository.existsByVolunteerIdAndRole(idVolunteer, VolunteerRole.VOLUNTEER)){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        }

        Optional<Volunteer> volunteer = volunteerRepository.findById(idVolunteer);
        if (volunteer.isPresent()) {
            volunteerService.degradeLeader(idVolunteer);
            logService.logVolunteer(volunteer.get(),EventType.UPDATE,"Degraded by admin with id: " + request.adminId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{idVolunteer}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable Long idVolunteer, @RequestBody AdminRequest request) {
        if (!volunteerRepository.existsByVolunteerIdAndRole(request.adminId(), VolunteerRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Volunteer> volunteer = volunteerRepository.findById(idVolunteer);
        if (volunteer.isPresent()) {
            volunteerRepository.deleteById(idVolunteer);

            logService.logVolunteer(volunteer.get(),EventType.DELETE,"Volunteer deleted by admin with id: " + request.adminId());

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }



}
