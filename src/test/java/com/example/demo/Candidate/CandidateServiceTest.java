package com.example.demo.Candidate;

import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Candidate.CandidateRepository;
import com.example.demo.Volunteer.Candidate.CandidateService;
import com.example.demo.Volunteer.VolunteerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CandidateServiceTest {

    @Mock
    private VolunteerService volunteerService;

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private CandidateService candidateService;

//    @Test
//    public void testAcceptCandidate() {
//        Candidate candidate = new Candidate();
//        Optional<Candidate> candidateOptional = Optional.of(candidate);
//
//        candidateService.acceptCandidate(candidateOptional);
//
//        verify(volunteerService, times(1)).addVolunteerFromCandidate(candidateOptional);
//        verify(candidateRepository, times(1)).delete(candidate);
//    }

//    @Test
//    public void testRefuseCandidate() {
//        Candidate candidate = new Candidate();
//        Optional<Candidate> candidateOptional = Optional.of(candidate);
//
//        candidateService.refuseCandidate(candidateOptional);
//
//        verify(candidateRepository, times(1)).delete(candidate);
//    }
}
