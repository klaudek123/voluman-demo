package com.example.demo.Candidate;

import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Candidate.CandidateController;
import com.example.demo.Volunteer.Candidate.CandidateRepository;
import com.example.demo.Volunteer.Candidate.CandidateService;
import com.example.demo.Volunteer.VolunteerRepository;
import com.example.demo.Volunteer.Role.VolunteerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class CandidateControllerTest {
    @InjectMocks
    private CandidateController candidateController;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private CandidateService candidateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testGetCandidates_ReturnsForbidden_WhenVolunteerIsNotRecruiter() {
        Long recruiterId = 1L;

        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(false);

        ResponseEntity<List<Candidate>> response = candidateController.getCandidates(recruiterId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
        verify(candidateRepository, never()).findAll();
    }
    @Test
    void testGetCandidates_ReturnsNotFound_WhenCandidateIsNotFound() {
        // Arrange
        Long recruiterId = 1L;
        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
        when(candidateRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Candidate>> response = candidateController.getCandidates(recruiterId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Optional check to verify the body content
        verify(candidateRepository, times(1)).findAll();
    }


    @Test
    void testGetCandidates_ReturnsOk_WhenVolunteerIsRecruiter() {
        Long recruiterId = 1L;

        Candidate candidate1 = new Candidate(1L, "John", "Doe", "john@example.com", "123456789", LocalDate.of(90, 1, 1), "Street", "City", "1", "2", "12345", "Male");
        Candidate candidate2 = new Candidate(2L, "Jane", "Doe", "jane@example.com", "987654321", LocalDate.of(91, 2, 2), "Street", "City", "1", "2", "12345", "Female");
        List<Candidate> candidates = Arrays.asList(candidate1, candidate2);

        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
        when(candidateRepository.findAll()).thenReturn(candidates);

        ResponseEntity<List<Candidate>> response = candidateController.getCandidates(recruiterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(candidates, response.getBody());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
        verify(candidateRepository, times(1)).findAll();
    }

    @Test
    void testGetCandidate_ReturnsForbidden_WhenVolunteerIsNotRecruiter() {
        Long recruiterId = 1L;
        long candidateId = 1L;


        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(false);

        ResponseEntity<Candidate> response = candidateController.getCandidate(candidateId, recruiterId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
        verify(candidateRepository, never()).findById(candidateId);
    }

    @Test
    void testGetCandidate_ReturnsNotFound_WhenCandidateNotExists() {
        Long recruiterId = 1L;
        Long candidateId = 1L;


        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.empty());

        ResponseEntity<Candidate> response = candidateController.getCandidate(candidateId, recruiterId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
        verify(candidateRepository, times(1)).findById(candidateId);
    }

    @Test
    void testGetCandidate_ReturnsOk_WhenCandidateFound() {
        Long recruiterId = 1L;
        Long candidateId = 1L;

        Candidate candidate = new Candidate(candidateId, "John", "Doe", "john@example.com", "123456789", LocalDate.of(90, 1, 1), "Street", "City", "1", "2", "12345", "Male");

        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));

        ResponseEntity<Candidate> response = candidateController.getCandidate(candidateId, recruiterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(candidate, response.getBody());
        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
        verify(candidateRepository, times(1)).findById(candidateId);
    }

//    @Test
//    void testAddCandidate_ReturnsCreated_WhenCandidateIsCreated() {
//        Candidate candidate = new Candidate(1L, "John", "Doe", "john@example.com", "123456789",
//                LocalDate.of(1990, 1, 1), "Street", "City", "1", "2", "12345", "M");
//
//        when(candidateRepository.save(candidate)).thenReturn(candidate);
//
//        ResponseEntity<Candidate> response = candidateController.   addCandidate(candidate);
//
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        assertEquals(candidate, response.getBody());
//        verify(candidateRepository, times(1)).save(candidate);
//    }

//    @Test
//    void testAcceptCandidate_ReturnsForbidden_WhenVolunteerIsNotRecruiter() {
//        Long recruiterId = 1L;
//        Long candidateId = 1L;
//
//
//        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(false);
//
//        ResponseEntity<Candidate> response = candidateController.acceptCandidate(candidateId, recruiterId);
//
//        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
//        verify(candidateRepository, never()).findById(candidateId);
//        verify(candidateService, never()).acceptCandidate(any(), any());
//    }

//    @Test
//    void testAcceptCandidate_ReturnsNotFound_WhenCandidateNotFound() {
//        Long recruiterId = 1L;
//        Long candidateId = 1L;
//
//
//        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
//        when(candidateRepository.findById(candidateId)).thenReturn(Optional.empty());
//
//        ResponseEntity<Candidate> response = candidateController.acceptCandidate(candidateId, recruiterId);
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
//        verify(candidateRepository, times(1)).findById(candidateId);
//        verify(candidateService, never()).acceptCandidate(any(), any());
//    }

//    @Test
//    void testAcceptCandidate_ReturnsOk_WhenRecruiterAcceptCandidate() {
//        Long recruiterId = 1L;
//        Long candidateId = 1L;
//
//        Candidate candidate = new Candidate(candidateId, "John", "Doe", "john@example.com", "123456789", LocalDate.of(90, 1, 1), "Street", "City", "1", "2", "12345", "Male");
//
//        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
//        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
//
//        ResponseEntity<Candidate> response = candidateController.acceptCandidate(candidateId, recruiterId);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(candidate, response.getBody());
//        verify(candidateService, times(1)).acceptCandidate(candidate.getCandidateId(), recruiterId);
//    }

//    @Test
//    void testRefuseCandidate_ReturnsForbidden_WhenVolunteerIsNotRecruiter() {
//        Long recruiterId = 1L;
//        Long candidateId = 1L;
//
//
//        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(false);
//
//        ResponseEntity<Candidate> response = candidateController.refuseCandidate(candidateId, recruiterId);
//
//        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
//        verify(candidateRepository, never()).findById(candidateId);
//        verify(candidateService, never()).refuseCandidate(any());
//    }

//    @Test
//    void testRefuseCandidate_ReturnsNotFound_WhenCandidateNotFound() {
//        Long recruiterId = 1L;
//        Long candidateId = 1L;
//
//
//        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
//        when(candidateRepository.findById(candidateId)).thenReturn(Optional.empty());
//
//        ResponseEntity<Candidate> response = candidateController.refuseCandidate(candidateId, recruiterId);
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        verify(volunteerRepository, times(1)).existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER);
//        verify(candidateRepository, times(1)).findById(candidateId);
//        verify(candidateService, never()).refuseCandidate(any());
//    }

//    @Test
//    void testRefuseCandidate_ReturnsOk_WhenRecruiterRefuseCandidate() {
//        Long recruiterId = 1L;
//        Long candidateId = 1L;
//
//        Candidate candidate = new Candidate(candidateId, "John", "Doe", "john@example.com", "123456789", LocalDate.of(90, 1, 1), "Street", "City", "1", "2", "12345", "Male");
//
//        when(volunteerRepository.existsByVolunteerIdAndRole(recruiterId, VolunteerRole.RECRUITER)).thenReturn(true);
//        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
//
//        ResponseEntity<Candidate> response = candidateController.refuseCandidate(candidateId, recruiterId);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(candidate, response.getBody());
//        verify(candidateService, times(1)).refuseCandidate(Optional.of(candidate));
//    }
}
