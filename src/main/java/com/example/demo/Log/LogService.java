package com.example.demo.Log;

import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerDetails;
import com.example.demo.Volunteer.VolunteerRepository;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LogService {
    private final LogRepository logRepository;
    private final VolunteerRepository volunteerRepository;

    public LogService(LogRepository logRepository, VolunteerRepository volunteerRepository) {
        this.logRepository = logRepository;
        this.volunteerRepository = volunteerRepository;
    }

    private LogResult log(LogUserDto logUserDto, EventType eventType, String eventDesc) {
        try {
            Log log = new Log();
            log.setFirstName(logUserDto.firstName());
            log.setLastName(logUserDto.lastName());
            log.setEmail(logUserDto.email());
            log.setEventType(eventType);
            log.setEventDesc(eventDesc);
            log.setTimestamp(LocalDateTime.now());

            logRepository.save(log);
            return new LogResult(true, null);
        } catch (Exception e) {
            return new LogResult(false, e.getMessage());
        }
    }

    public void logCandidate(Candidate candidate, EventType eventType, String eventDesc) {
        try {

            LogResult result = log(
                    new LogUserDto(candidate.getFirstname(), candidate.getLastname(), candidate.getEmail()),
                    eventType,
                    eventDesc
            );

            if (!result.isSuccess()) {
                throw new RuntimeException(result.getErrorMessage());
            }
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void logVolunteer(Volunteer volunteer, EventType eventType, String eventDesc) {
        try {
            LogResult result = log(
                    new LogUserDto(
                            volunteer.getVolunteerDetails().getFirstname(),
                            volunteer.getVolunteerDetails().getLastname(),
                            volunteer.getVolunteerDetails().getEmail()
                    ),
                    eventType,
                    eventDesc
            );

            if (!result.isSuccess()) {
                throw new RuntimeException(result.getErrorMessage());
            }
        } catch (RuntimeException e) {
            System.out.println("Error during logging: " + e.getMessage());
        }
    }


    public void logAction(Long volunteerId, EventType eventType, String eventDesc) {
        try {
            Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);
            VolunteerDetails volunteerDetails = volunteer.get().getVolunteerDetails();


            LogResult result = log(
                    new LogUserDto(
                            volunteerDetails.getFirstname(),
                            volunteerDetails.getLastname(),
                            volunteerDetails.getEmail()
                    ),
                    eventType,
                    eventDesc
            );

            if (!result.isSuccess()) {
                throw new RuntimeException(result.getErrorMessage());
            }
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void logSchedule(Long volunteerId, EventType eventType, String eventDesc) {
        try {
            Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);
            VolunteerDetails volunteerDetails = volunteer.get().getVolunteerDetails();

            LogResult result = log(
                    new LogUserDto(
                            volunteerDetails.getFirstname(),
                            volunteerDetails.getLastname(),
                            volunteerDetails.getEmail()
                    ),
                    eventType,
                    eventDesc
            );

            if (!result.isSuccess()) {
                throw new RuntimeException(result.getErrorMessage());
            }
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
