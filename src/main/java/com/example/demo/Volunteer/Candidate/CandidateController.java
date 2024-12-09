package com.example.demo.Volunteer.Candidate;


import com.example.demo.Log.EventType;
import com.example.demo.Log.LogService;
import com.example.demo.Volunteer.User.UserRepository;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Volunteer.VolunteerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final VolunteerRepository volunteerRepository;
    private final LogService logService;

    public CandidateController(CandidateRepository candidateRepository, CandidateService candidateService, VolunteerRepository volunteerRepository, LogService logService) {
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.volunteerRepository = volunteerRepository;
        this.logService = logService;
    }

    @GetMapping("")
    public ResponseEntity<List<Candidate>> getCandidates(@RequestParam Long recruiterId) {
        if (!volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Candidate> candidates = candidateRepository.findAll();
        if (candidates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(candidates);
    }


    @GetMapping("/{idCandidate}")
    public ResponseEntity<Candidate> getCandidate(@PathVariable long idCandidate, @RequestParam Long recruiterId) { //DONE
        if (!volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Candidate> candidate = candidateRepository.findById(idCandidate);
        return candidate.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping("")
    public ResponseEntity<Candidate> addCandidate(@RequestBody Candidate candidate) {

        Candidate savedCandidate = candidateService.addCandidate(candidate);


        return ResponseEntity.status(HttpStatus.CREATED).body(savedCandidate);
    }

    @PostMapping("{idCandidate}/accept")
    public ResponseEntity<Candidate> acceptCandidate(@PathVariable long idCandidate, @RequestParam Long recruiterId) {
        Optional<Candidate> candidate = candidateRepository.findById(idCandidate);
        if (candidate.isPresent()) {
            candidateService.acceptCandidate(idCandidate, recruiterId);

            logService.logCandidate(candidate.get(), EventType.ACCEPT, "Candidate accepted by recruiter " + recruiterId);

            return ResponseEntity.ok(candidate.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("{idCandidate}/refuse")
    public ResponseEntity<Candidate> refuseCandidate(@PathVariable long idCandidate, @RequestParam Long recruiterId) {
        if (!volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Candidate> candidate = candidateRepository.findById(idCandidate);
        if (candidate.isPresent()) {
            candidateService.refuseCandidate(idCandidate, recruiterId);

            logService.logCandidate(candidate.get(), EventType.REFUSE, "Candidate refused by recruiter " + recruiterId);

            return ResponseEntity.ok(candidate.get());
        }
        return ResponseEntity.notFound().build();
    }

}
