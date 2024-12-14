package com.example.demo.Volunteer;

import com.example.demo.Volunteer.Duty.Duty;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyInterval;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyIntervalStatus;
import com.example.demo.Volunteer.Role.VolunteerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VolunteerTest {

    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        volunteer = new Volunteer();
        volunteer.setVolunteerId(1L);
        volunteer.setRole(VolunteerRole.VOLUNTEER);
        volunteer.setLimitOfWeeklyHours(40.0);
        volunteer.setCurrentWeeklyHours(0.0);
    }

    // calculateCurrentWeeklyHours

    @Test
    void testCalculateCurrentWeeklyHours_WithDutiesInWeek() {
        // Given
        LocalDate startOfWeek = LocalDate.of(2024, 6, 3);
        LocalDate endOfWeek = LocalDate.of(2024, 6, 9);

        Duty duty1 = createDutyWithIntervals(startOfWeek, 4);
        Duty duty2 = createDutyWithIntervals(startOfWeek.plusDays(2), 3);

        Set<Duty> duties = new HashSet<>();
        duties.add(duty1);
        duties.add(duty2);

        volunteer.setDuties(duties);

        // When
        double totalHours = volunteer.calculateCurrentWeeklyHours(startOfWeek, endOfWeek);

        // Then
        assertEquals(7.0, totalHours, 0.01);
    }

    @Test
    void testCalculateCurrentWeeklyHours_WithNoDutiesInWeek() {
        // Given
        LocalDate startOfWeek = LocalDate.of(2024, 6, 3);
        LocalDate endOfWeek = LocalDate.of(2024, 6, 9);

        // No duties assigned to the volunteer
        volunteer.setDuties(new HashSet<>());

        // When
        double totalHours = volunteer.calculateCurrentWeeklyHours(startOfWeek, endOfWeek);

        // Then
        assertEquals(0.0, totalHours, 0.01);
    }

    // prePersist

    @Test
    void testPrePersist_InitializesFieldsCorrectly() {
        // Given
        Volunteer newVolunteer = new Volunteer();
        newVolunteer.prePersist();

        // Then
        assertNotNull(newVolunteer.getPreferences());
        assertNotNull(newVolunteer.getActions());
        assertNotNull(newVolunteer.getDuties());
        assertNotNull(newVolunteer.getAvailabilities());
    }

    // setRole

    @Test
    void testSetRole() {
        // Given
        volunteer.setRole(VolunteerRole.LEADER);

        // Then
        assertEquals(VolunteerRole.LEADER, volunteer.getRole());
    }

    // Helper methods

    private Duty createDutyWithIntervals(LocalDate date, int hours) {
        Duty duty = new Duty();
        duty.setDate(date);

        DutyInterval interval = new DutyInterval();
        interval.setStartTime(LocalTime.of(9, 0));
        interval.setEndTime(LocalTime.of(9 + hours, 0));
        interval.setStatus(DutyIntervalStatus.ASSIGNED);
        interval.setDuty(duty);

        Set<DutyInterval> intervals = new HashSet<>();
        intervals.add(interval);

        duty.setDutyIntervals(intervals);

        return duty;
    }
}
