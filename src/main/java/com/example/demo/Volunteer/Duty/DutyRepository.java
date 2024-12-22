package com.example.demo.Volunteer.Duty;

import com.example.demo.Volunteer.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {
    List<Duty> findAllByDate(LocalDate date);

    List<Duty> findAllByVolunteerAndDateBetween(Volunteer volunteer, LocalDate startOfWeek, LocalDate endOfWeek);

    List<Duty> findByVolunteer_VolunteerIdAndDateBetween(Long volunteerId, LocalDate startDate, LocalDate endDate);

}
