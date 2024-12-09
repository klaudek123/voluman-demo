package com.example.demo.Schedule;

import com.example.demo.Action.ActionDto.ActionScheduleDto;
import com.example.demo.Action.ActionRepository;
import com.example.demo.Log.EventType;
import com.example.demo.Log.LogService;
import com.example.demo.Schedule.ScheduleDto.*;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Volunteer.VolunteerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("")
public class ScheduleController {
    private final LogService logService;
    private final ScheduleService scheduleService;
    private final ActionRepository actionRepository;
    private final VolunteerRepository volunteerRepository;

    public ScheduleController(ScheduleService scheduleService, ActionRepository actionRepository, VolunteerRepository volunteerRepository, LogService logService) {
        this.scheduleService = scheduleService;
        this.actionRepository = actionRepository;
        this.volunteerRepository = volunteerRepository;
        this.logService = logService;
    }


    @PostMapping("/actions/{actionId}/preferences")
    public ResponseEntity<?> choosePref(
            @PathVariable("actionId") Long actionId,
            @RequestBody ActionPrefRequest actionPrefRequest
    ) {
        try {
            if (!actionRepository.existsById(actionId)) {
                return ResponseEntity.notFound().build();
            }
            if (!volunteerRepository.existsById(actionPrefRequest.volunteerId())) {
                return ResponseEntity.notFound().build();
            }
            scheduleService.choosePref(actionId, actionPrefRequest);

            logService.logSchedule(actionPrefRequest.volunteerId(), EventType.UPDATE, "Choose preferences");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/actions/{actionId}/demands")
    public ResponseEntity<?> chooseDemands(
            @PathVariable("actionId") Long actionId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int week,
            @RequestBody ActionNeedRequest actionNeedRequest
    ) {
        try {
            if (!actionRepository.existsById(actionId)) {
                return ResponseEntity.notFound().build();
            }
            if (!volunteerRepository.existsByVolunteerIdAndRole(actionNeedRequest.getLeaderId(), VolunteerRole.LEADER)) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (!Objects.equals(actionRepository.findById(actionId).get().getLeader().leaderId(), actionNeedRequest.getLeaderId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            scheduleService.scheduleNeedAction(actionId, year, week, actionNeedRequest);

            logService.logSchedule(actionNeedRequest.getLeaderId(), EventType.UPDATE, "Choose action needs with id: " + actionId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/volunteers/{volunteerId}/availabilities")
    public ResponseEntity<?> chooseAvail(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int week,
            @RequestBody VolunteerAvailRequest volunteerAvailRequest
    ) {
        try {
            if (!volunteerRepository.existsById(volunteerId)) {
                return ResponseEntity.notFound().build();
            }
            scheduleService.chooseAvailabilities(volunteerId, year, week, volunteerAvailRequest);

            logService.logSchedule(volunteerId, EventType.UPDATE, "Choose availabilities");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/schedules/generate")
    public ResponseEntity<?> generateSchedule(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int week,
            @RequestBody GenerateScheduleRequest generateScheduleRequest
    ) {
        try {
            // Walidacja: Sprawdzenie, czy użytkownik jest administratorem
            if (!volunteerRepository.existsByVolunteerIdAndRole(generateScheduleRequest.adminId(), VolunteerRole.ADMIN)) {
                return ResponseEntity.notFound().build();
            }

            // Obliczanie oczekiwanej daty na podstawie roku i tygodnia
            LocalDate expectedDate = LocalDate.ofYearDay(year, 1).plusWeeks(week - 1);
            LocalDate requestDate = generateScheduleRequest.date();

            // Sprawdzenie, czy data w żądaniu zgadza się z oczekiwaną datą
            if (!expectedDate.equals(requestDate)) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(expectedDate);
            }

            // Wywołanie usługi generowania harmonogramu
            scheduleService.generateSchedule(generateScheduleRequest.date());

            logService.logSchedule(generateScheduleRequest.adminId(), EventType.CREATE, "Generate schedule");
            return ResponseEntity.ok().body("Schedule generated successfully.");
        } catch (Exception e) {
            // Obsługa wyjątków i zwrócenie odpowiedzi z kodem błędu 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating schedule.");
        }
    }


    @PutMapping("/volunteers/{volunteerId}/schedules/modify")
    public ResponseEntity<?> modifySchedule(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int week,
            @RequestBody ModifyScheduleRequest modifyScheduleRequest
    ) {
        try {
            if (!volunteerRepository.existsById(volunteerId)) {
                return ResponseEntity.notFound().build();
            }
            scheduleService.modifySchedule(volunteerId, year, week, modifyScheduleRequest);


            logService.logSchedule(volunteerId, EventType.UPDATE, "Schedule modified by volunteer with id: " + volunteerId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @GetMapping("/actions/{actionId}/schedules")
    public ResponseEntity<?> getScheduleByAction(
            @PathVariable Long actionId,
            @RequestParam(defaultValue = "0") Long leaderId
    ) {
        //TODO w przyszłości rozbicie logiki na get od wolontariusza i od leadera
        try {
            if (!actionRepository.existsById(actionId)) {
                return ResponseEntity.notFound().build();
            }
            if (!volunteerRepository.existsByVolunteerIdAndRole(leaderId, VolunteerRole.LEADER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (!Objects.equals(actionRepository.findById(actionId).get().getLeader().leaderId(), leaderId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            ActionScheduleDto scheduleDto = scheduleService.getScheduleByAction(actionId);
            return ResponseEntity.ok(scheduleDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/volunteers/{volunteerId}/schedules")
    public ResponseEntity<?> getScheduleByVolunteer(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int week
    ) {
        try {
            if (!volunteerRepository.existsById(volunteerId)) {
                return ResponseEntity.notFound().build();
            }

            VolunteerScheduleDto scheduleDto = scheduleService.getScheduleByVolunteer(volunteerId, year, week);
            return ResponseEntity.ok(scheduleDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}



