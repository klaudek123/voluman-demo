package com.example.demo.Schedule;

import com.example.demo.Action.Demand.DemandInterval.DemandInterval;
import com.example.demo.Action.Demand.DemandInterval.DemandIntervalDto;
import com.example.demo.Volunteer.Preferences.Preferences;
import com.example.demo.Schedule.ScheduleDto.ActionNeedRequest;
import com.example.demo.Schedule.ScheduleDto.ModifyScheduleRequest;
import com.example.demo.Schedule.ScheduleDto.VolunteerAvailRequest;
import com.example.demo.Schedule.ScheduleDto.VolunteerScheduleDto;
import com.example.demo.Volunteer.Availability.Availability;
import com.example.demo.Volunteer.Availability.AvailabilityService;
import com.example.demo.Volunteer.Availability.AvailabilityInterval.AvailabilityInterval;
import com.example.demo.Volunteer.Duty.Duty;
import com.example.demo.Volunteer.Duty.DutyRepository;
import com.example.demo.Volunteer.Duty.DutyService;
import com.example.demo.Volunteer.*;
import com.example.demo.Action.Action;
import com.example.demo.Action.ActionRepository;
import com.example.demo.Action.ActionService;
import com.example.demo.Action.ActionDto.ActionScheduleDto;
import com.example.demo.Action.Demand.Demand;
import com.example.demo.Action.Demand.DemandDto;
import com.example.demo.Action.Demand.DemandRepository;
import com.example.demo.Action.Demand.DemandService;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyInterval;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyIntervalDto;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyIntervalRepository;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyIntervalStatus;
import com.example.demo.Volunteer.VolunteerDto.VolunteerDto;
import com.example.demo.Volunteer.Role.VolunteerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class ScheduleServiceTest {
    @Mock
    private DutyIntervalRepository dutyIntervalRepository;

    @Mock
    private ActionService actionService;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private VolunteerService volunteerService;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private DemandService demandService;

    @Mock
    private DutyService dutyService;

    @Mock
    private DemandRepository demandRepository;

    @InjectMocks
    private ScheduleService scheduleService;


    @Mock
    private DutyRepository dutyRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testScheduleNeedAction_LeaderValidationFailure() {
        ActionNeedRequest request = new ActionNeedRequest();
        request.setLeaderId(1L);

        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(false);

        assertThrows(Exception.class, () -> scheduleService.scheduleNeedAction(1L, 2024, 27, request));
    }

    @Test
    void testScheduleNeedAction_ActionNotFound() {
        ActionNeedRequest request = new ActionNeedRequest();
        request.setLeaderId(1L);

        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(true);
        when(actionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> scheduleService.scheduleNeedAction(1L, 2024, 27, request));
    }

    @Test
    void testScheduleNeedAction_DateValidationFailure() {
        ActionNeedRequest request = new ActionNeedRequest();
        request.setLeaderId(1L);
        ActionNeedRequest.DayRequest dayRequest = new ActionNeedRequest.DayRequest();
        dayRequest.setDate(LocalDate.of(2024, 7, 4));
        request.setDays(List.of(dayRequest));

        Action action = new Action();
        action.setStartDay(LocalDate.of(2024, 7, 5));
        action.setEndDay(LocalDate.of(2024, 7, 10));

        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(true);
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        assertThrows(Exception.class, () -> scheduleService.scheduleNeedAction(1L, 2024, 27, request));
    }

    @Test
    void testScheduleNeedAction_Success() throws Exception {
        ActionNeedRequest request = new ActionNeedRequest();
        request.setLeaderId(1L);
        ActionNeedRequest.DayRequest dayRequest = new ActionNeedRequest.DayRequest();
        dayRequest.setDate(LocalDate.of(2024, 7, 4));
        ActionNeedRequest.SlotRequest slotRequest = new ActionNeedRequest.SlotRequest();
        slotRequest.setStartTime(LocalTime.of(9, 0));
        slotRequest.setEndTime(LocalTime.of(9, 30));
        slotRequest.setNeedMin(1L);
        slotRequest.setNeedMax(5L);
        dayRequest.setSlots(List.of(slotRequest));
        request.setDays(List.of(dayRequest));

        Action action = new Action();
        action.setStartDay(LocalDate.of(2024, 7, 1));
        action.setEndDay(LocalDate.of(2024, 7, 31));
        action.setDemands(new ArrayList<>()); // Initialize demands list

        when(volunteerRepository.existsByVolunteerIdAndRole(1L, VolunteerRole.LEADER)).thenReturn(true);
        when(actionRepository.findById(1L)).thenReturn(Optional.of(action));

        scheduleService.scheduleNeedAction(1L, 2024, 27, request);

        verify(actionRepository, times(1)).save(any(Action.class));
        verify(demandService, times(1)).addDemand(any(Demand.class));
    }

    @Test
    void testChooseAvailabilities_VolunteerNotFound() {
        VolunteerAvailRequest request = new VolunteerAvailRequest();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> scheduleService.chooseAvailabilities(1L, 2024, 27, request));
    }

    @Test
    void testChooseAvailabilities_DateValidationFailure() {
        VolunteerAvailRequest request = new VolunteerAvailRequest();
        VolunteerAvailRequest.DayAvailabilityRequest dayRequest = new VolunteerAvailRequest.DayAvailabilityRequest();
        dayRequest.setDate(LocalDate.of(2024, 8, 4)); // Date not matching the specified week
        request.setDays(List.of(dayRequest));

        Volunteer volunteer = new Volunteer();

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));

        assertThrows(Exception.class, () -> scheduleService.chooseAvailabilities(1L, 2024, 27, request));
    }

    @Test
    void testChooseAvailabilities_Success() throws Exception {
        VolunteerAvailRequest request = new VolunteerAvailRequest();
        request.setLimitOfHours(10L);
        VolunteerAvailRequest.DayAvailabilityRequest dayRequest = new VolunteerAvailRequest.DayAvailabilityRequest();
        dayRequest.setDate(LocalDate.of(2024, 7, 5));
        VolunteerAvailRequest.AvailabilitySlotRequest slotRequest = new VolunteerAvailRequest.AvailabilitySlotRequest();
        slotRequest.setStartTime(LocalTime.of(9, 0));
        slotRequest.setEndTime(LocalTime.of(17, 0));
        dayRequest.setSlots(List.of(slotRequest));
        request.setDays(List.of(dayRequest));

        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerId(1L);
        volunteer.setAvailabilities(new ArrayList<>());

        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(availabilityService.getByVolunteerIdAndDate(1L, LocalDate.of(2024, 7, 5))).thenReturn(new Availability());

        scheduleService.chooseAvailabilities(1L, 2024, 27, request);

        verify(volunteerRepository, times(1)).save(any(Volunteer.class));
        verify(availabilityService, times(1)).addAvail(any(Availability.class));
    }

    @Test
    void testGenerateSchedule() {
        LocalDate date = LocalDate.now();

        // Mocking availabilities
        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerId(1L);
        volunteer.setLimitOfWeeklyHours(10);
        volunteer.setDuties(new HashSet<>());


        Availability availability = new Availability();
        availability.setAvailabilityId(1L);
        availability.setVolunteer(volunteer);
        availability.setDate(date);

        AvailabilityInterval interval = new AvailabilityInterval();
        interval.setIntervalId(1L);
        interval.setStartTime(LocalTime.of(9, 0));
        interval.setEndTime(LocalTime.of(11, 0));
        availability.setSlots(Set.of(interval));

        when(availabilityService.getAvailabilitiesForDay(date)).thenReturn(List.of(availability));

        // Mocking demands
        Action action = new Action();
        action.setActionId(1L);
        action.setStartDay(date.minusDays(3));
        action.setEndDay(date.plusDays(3));
//        action.setLeader(new LeaderD);

        Preferences preferences = new Preferences();
        preferences.setPreferenceId(1L);
        preferences.getT().add(action);

        volunteer.setPreferences(preferences);

        Demand demand = new Demand();
        demand.setAction(action);
        demand.setDate(date);

        DemandInterval demandInterval = new DemandInterval();
        demandInterval.setStartTime(LocalTime.of(9, 0));
        demandInterval.setEndTime(LocalTime.of(11, 0));
        demandInterval.setNeedMax(1L);
        demandInterval.setCurrentVolunteersNumber(0L);
        demandInterval.setDemand(demand);
        demand.setDemandIntervals(Set.of(demandInterval));

        when(demandService.getDemandsForDay(date)).thenReturn(List.of(demand));
        when(actionRepository.findById(any())).thenReturn(Optional.of(action));
        when(volunteerRepository.findAll()).thenReturn(List.of(volunteer));

        doNothing().when(dutyService).addDutyInterval(any(DutyInterval.class), any(Duty.class));
        doNothing().when(dutyService).updateDutyInterval(any(DutyInterval.class));

        // Call the method
        scheduleService.generateSchedule(date);

        // Verify the interactions
        verify(demandService, times(1)).getDemandsForDay(date);
        verify(availabilityService, times(1)).getAvailabilitiesForDay(date);
        // Verify that save was called on the dutyIntervalRepository with any instance of DutyInterval
        //  verify(dutyIntervalRepository, times(1)).save(new DutyInterval());

        verify(dutyService, times(1)).addDutyInterval(any(DutyInterval.class), any(Duty.class));


        // Assert that exactly one of these methods was called, not both
        verify(dutyService, atMost(1)).addDutyInterval(any(DutyInterval.class), any(Duty.class));
        verify(dutyService, atMost(1)).updateDutyInterval(any(DutyInterval.class));

        // Additional verifications for other methods if needed
        verify(volunteerRepository, times(2)).save(volunteer);
    }

    @Test
    public void testModifySchedule_Success() {
        // Setup test data
        Long volunteerId = 1L;
        int year = 2023;
        int week = 30;

        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerId(volunteerId);
        volunteer.setCurrentWeeklyHours(10.0);

        Action action = new Action();
        action.setActionId(1L);

        volunteer.setActions(new HashSet<>(Collections.singletonList(action)));

        Duty duty = new Duty();
        duty.setVolunteer(volunteer);
        duty.setDutyIntervals(new HashSet<>());
        duty.setDate(LocalDate.ofYearDay(year, (week - 1) * 7 + 1)); // Adjust to match the setup in modifySchedule

        DutyInterval interval1 = new DutyInterval();
        interval1.setIntervalId(100L);
        interval1.setStatus(DutyIntervalStatus.ASSIGNED);
        interval1.setStartTime(LocalTime.of(9, 0));
        interval1.setEndTime(LocalTime.of(12, 0));
        interval1.setDuty(duty);

        duty.getDutyIntervals().add(interval1);

        // Mock repository responses
        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(volunteer));
        when(dutyRepository.findAllByVolunteerAndDateBetween(any(Volunteer.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(duty));

        // Mock demand service
        Demand demand = new Demand();
        demand.setAction(action);

        DemandInterval demandInterval = new DemandInterval();
        demandInterval.setStartTime(LocalTime.of(9, 0));
        demandInterval.setEndTime(LocalTime.of(12, 0));
        demandInterval.setCurrentVolunteersNumber(1L);
        demandInterval.setDemand(demand);

        // Note: You can initialize the set with Collections.singleton for simplicity
        demand.setDemandIntervals(new HashSet<>(Collections.singleton(demandInterval)));

        when(demandService.findAllByActionId(any(Long.class))).thenReturn(Collections.singletonList(demand));

        // Prepare request
        ModifyScheduleRequest modifyScheduleRequest = new ModifyScheduleRequest(1L, new ArrayList<>(duty.getDutyIntervals()));

        // Execute the service method
        scheduleService.modifySchedule(volunteerId, year, week, modifyScheduleRequest);

        // Verify results
        assertEquals(7.0, volunteer.getCurrentWeeklyHours());
        assertEquals(DutyIntervalStatus.CANCELED, interval1.getStatus());
        assertEquals(0L, demandInterval.getCurrentVolunteersNumber());

        // Verify repository save calls
        verify(dutyRepository, times(1)).saveAll(anyList());
        verify(volunteerRepository, times(1)).save(any(Volunteer.class));
    }


    @Test
    public void testGetScheduleByVolunteer_Success() {
        // Setup test data
        Long volunteerId = 1L;
        int year = 2023;
        int week = 30;

        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerId(volunteerId);

        VolunteerDetails details = new VolunteerDetails();
        details.setFirstname("John");
        details.setLastname("Doe");
        volunteer.setVolunteerDetails(details);

        LocalDate startDate = LocalDate.ofYearDay(year, 1).with(WeekFields.of(Locale.getDefault()).weekOfYear(), week);
        LocalDate endDate = startDate.plusDays(6);

        // Create multiple duties and demands for different days
        List<Duty> duties = new ArrayList<>();
        List<Demand> demands = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            LocalDate dutyDate = startDate.plusDays(i);
            LocalDate demandDate = startDate.plusDays(i);

            Duty duty = new Duty();
            duty.setVolunteer(volunteer);
            duty.setDate(dutyDate);
            duty.setDutyIntervals(new HashSet<>());

            DutyInterval dutyInterval = new DutyInterval();
            dutyInterval.setIntervalId(1L + i);
            dutyInterval.setStartTime(LocalTime.of(9, 0));
            dutyInterval.setEndTime(LocalTime.of(12, 0));
            dutyInterval.setDuty(duty);

            duty.getDutyIntervals().add(dutyInterval);
            duties.add(duty);

            Action action = new Action();
            action.setActionId(1L + i);
            action.setHeading("Help Elderly " + i);

            Demand demand = new Demand();
            demand.setAction(action);
            demand.setDate(demandDate);
            demand.setDemandIntervals(new HashSet<>());

            DemandInterval demandInterval = new DemandInterval();
            demandInterval.setStartTime(LocalTime.of(9, 0));
            demandInterval.setEndTime(LocalTime.of(12, 0));
            demandInterval.setDemand(demand);

            demand.getDemandIntervals().add(demandInterval);
            demands.add(demand);
        }

        // Mock repository responses
        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(volunteer));
        when(dutyRepository.findByVolunteer_VolunteerIdAndDateBetween(volunteerId, startDate, endDate))
                .thenReturn(duties);
        when(demandRepository.findByDateBetween(startDate, endDate))
                .thenReturn(demands);

        // Execute the service method
        VolunteerScheduleDto scheduleDto = scheduleService.getScheduleByVolunteer(volunteerId, year, week);

        // Verify the results
        assertNotNull(scheduleDto);
        assertEquals(volunteerId, scheduleDto.volunteerId());
        assertEquals("John", scheduleDto.name());
        assertEquals("Doe", scheduleDto.lastname());

        List<DutyIntervalDto> dutyIntervals = scheduleDto.dutyIntervals();
        assertNotNull(dutyIntervals);
        assertEquals(3, dutyIntervals.stream().map(DutyIntervalDto::intervalId).distinct().count());

        for (Duty duty : duties) {
            for (DutyInterval dutyInterval : duty.getDutyIntervals()) {
                boolean matchFound = dutyIntervals.stream().anyMatch(dutyIntervalDto ->
                        dutyIntervalDto.intervalId().equals(dutyInterval.getIntervalId()) &&
                                dutyIntervalDto.startTime().equals(dutyInterval.getStartTime()) &&
                                dutyIntervalDto.endTime().equals(dutyInterval.getEndTime())
                );
                assertTrue(matchFound, "Matching DutyIntervalDto not found for DutyInterval: " + dutyInterval);
            }
        }

        // Verify repository calls
        verify(volunteerRepository, times(1)).findById(volunteerId);
        verify(dutyRepository, times(1)).findByVolunteer_VolunteerIdAndDateBetween(volunteerId, startDate, endDate);
        verify(demandRepository, times(1)).findByDateBetween(startDate, endDate);
    }

    @Test
    public void testGetScheduleByVolunteer_Success_One() {
        // Setup test data
        Long volunteerId = 1L;
        int year = 2023;
        int week = 30;

        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerId(volunteerId);

        VolunteerDetails details = new VolunteerDetails();
        details.setFirstname("John");
        details.setLastname("Doe");
        volunteer.setVolunteerDetails(details);

        LocalDate startDate = LocalDate.ofYearDay(year, 1).with(WeekFields.of(Locale.getDefault()).weekOfYear(), week);
        LocalDate endDate = startDate.plusDays(6);

        Duty duty = new Duty();
        duty.setVolunteer(volunteer);
        duty.setDate(startDate);
        duty.setDutyIntervals(new HashSet<>());

        DutyInterval dutyInterval = new DutyInterval();
        dutyInterval.setIntervalId(100L);
        dutyInterval.setStartTime(LocalTime.of(9, 0));
        dutyInterval.setEndTime(LocalTime.of(12, 0));
        dutyInterval.setDuty(duty);

        duty.getDutyIntervals().add(dutyInterval);

        Action action = new Action();
        action.setActionId(1L);
        action.setHeading("Help Elderly");

        Demand demand = new Demand();
        demand.setAction(action);
        demand.setDate(startDate);
        demand.setDemandIntervals(new HashSet<>());

        DemandInterval demandInterval = new DemandInterval();
        demandInterval.setStartTime(LocalTime.of(9, 0));
        demandInterval.setEndTime(LocalTime.of(12, 0));
        demandInterval.setDemand(demand);

        demand.getDemandIntervals().add(demandInterval);

        // Mock repository responses
        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(volunteer));
        when(dutyRepository.findByVolunteer_VolunteerIdAndDateBetween(volunteerId, startDate, endDate))
                .thenReturn(Collections.singletonList(duty));
        when(demandRepository.findByDateBetween(startDate, endDate))
                .thenReturn(Collections.singletonList(demand));

        // Execute the service method
        VolunteerScheduleDto scheduleDto = scheduleService.getScheduleByVolunteer(volunteerId, year, week);

        // Verify the results
        assertNotNull(scheduleDto);
        assertEquals(volunteerId, scheduleDto.volunteerId());
        assertEquals("John", scheduleDto.name());
        assertEquals("Doe", scheduleDto.lastname());

        List<DutyIntervalDto> dutyIntervals = scheduleDto.dutyIntervals();
        assertNotNull(dutyIntervals);
        assertEquals(1, dutyIntervals.size());

        DutyIntervalDto dutyIntervalDto = dutyIntervals.get(0);
        assertEquals(dutyInterval.getIntervalId(), dutyIntervalDto.intervalId());
        assertEquals(startDate, dutyIntervalDto.date());
        assertEquals(dutyInterval.getStartTime(), dutyIntervalDto.startTime());
        assertEquals(dutyInterval.getEndTime(), dutyIntervalDto.endTime());
        assertEquals(action.getActionId(), dutyIntervalDto.action().actionId());
        assertEquals(action.getHeading(), dutyIntervalDto.action().heading());

        // Verify repository calls
        verify(volunteerRepository, times(1)).findById(volunteerId);
        verify(dutyRepository, times(1)).findByVolunteer_VolunteerIdAndDateBetween(volunteerId, startDate, endDate);
        verify(demandRepository, times(1)).findByDateBetween(startDate, endDate);
    }
    @Test
    void testGetScheduleByAction_One() {
        // Create mock data
        Long actionId = 1L;

        Action action = new Action();
        action.setActionId(actionId);
        action.setHeading("Test Action");
        action.setDescription("Test Description");

        Demand demand = new Demand();
        demand.setDemandId(1L);
        demand.setDate(LocalDate.of(2024, 7, 20));
        DemandInterval demandInterval = new DemandInterval();
        demandInterval.setIntervalId(1L);
        demandInterval.setStartTime(LocalTime.of(9, 0));
        demandInterval.setEndTime(LocalTime.of(12, 0));
        demandInterval.setDemand(demand);
        demand.setDemandIntervals(Set.of(demandInterval));

        action.setDemands(List.of(demand));

        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerId(1L);
        VolunteerDetails details = new VolunteerDetails();
        details.setFirstname("John");
        details.setLastname("Doe");
        volunteer.setVolunteerDetails(details);

        Duty duty = new Duty();
        duty.setVolunteer(volunteer);
        duty.setDate(LocalDate.of(2024, 7, 20));
        DutyInterval dutyInterval = new DutyInterval();
        dutyInterval.setIntervalId(1L);
        dutyInterval.setStartTime(LocalTime.of(9, 0));
        dutyInterval.setEndTime(LocalTime.of(12, 0));
        dutyInterval.setDuty(duty);
        duty.setDutyIntervals(Set.of(dutyInterval));

        // Mock the behavior of repositories
        when(actionRepository.findById(actionId)).thenReturn(Optional.of(action));
        when(dutyRepository.findAll()).thenReturn(List.of(duty));

        // Call the method to be tested
        ActionScheduleDto result = scheduleService.getScheduleByAction(actionId);

        // Verify the result
        assertNotNull(result);
        assertEquals(actionId, result.actionId());
        assertEquals("Test Action", result.heading());
        assertEquals("Test Description", result.description());

        List<DemandDto> demandDtos = result.demands();
        assertNotNull(demandDtos);
        assertEquals(1, demandDtos.size());

        DemandDto demandDto = demandDtos.get(0);
        assertEquals(1L, demandDto.demandId());
        assertEquals(LocalDate.of(2024, 7, 20), demandDto.date());

        List<DemandIntervalDto> intervalDtos = demandDto.demandIntervals();
        assertNotNull(intervalDtos);
        assertEquals(1, intervalDtos.size());

        DemandIntervalDto intervalDto = intervalDtos.get(0);
        assertEquals(1L, intervalDto.intervalId());
        assertEquals(LocalTime.of(9, 0), intervalDto.startTime());
        assertEquals(LocalTime.of(12, 0), intervalDto.endTime());

        List<VolunteerDto> assignedVolunteers = intervalDto.assignedVolunteers();
        assertNotNull(assignedVolunteers);
        assertEquals(1, assignedVolunteers.size());

        VolunteerDto volunteerDto = assignedVolunteers.get(0);
        assertEquals(1L, volunteerDto.volunteerId());
        assertEquals("John", volunteerDto.firstname());
        assertEquals("Doe", volunteerDto.lastname());

        // Verify the interactions with the repositories
        verify(actionRepository, times(1)).findById(actionId);
        verify(dutyRepository, times(1)).findAll();
    }

    @Test
    void testGetScheduleByAction_ThreeDutiesThreeDemands() {
        // Create mock data
        Long actionId = 1L;

        Action action = new Action();
        action.setActionId(actionId);
        action.setHeading("Test Action");
        action.setDescription("Test Description");

        List<Demand> demands = new ArrayList<>();
        List<Duty> duties = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            LocalDate date = LocalDate.of(2024, 7, 20).plusDays(i);

            Demand demand = new Demand();
            demand.setDemandId((long) (i + 1));
            demand.setDate(date);
            DemandInterval demandInterval = new DemandInterval();
            demandInterval.setIntervalId((long) (i + 1));
            demandInterval.setStartTime(LocalTime.of(9, 0));
            demandInterval.setEndTime(LocalTime.of(12, 0));
            demandInterval.setDemand(demand);
            demand.setDemandIntervals(Set.of(demandInterval));
            demands.add(demand);

            Volunteer volunteer = new Volunteer();
            volunteer.setVolunteerId((long) (i + 1));
            VolunteerDetails details = new VolunteerDetails();
            details.setFirstname("John" + i);
            details.setLastname("Doe" + i);
            volunteer.setVolunteerDetails(details);

            Duty duty = new Duty();
            duty.setVolunteer(volunteer);
            duty.setDate(date);
            DutyInterval dutyInterval = new DutyInterval();
            dutyInterval.setIntervalId((long) (i + 1));
            dutyInterval.setStartTime(LocalTime.of(9, 0));
            dutyInterval.setEndTime(LocalTime.of(12, 0));
            dutyInterval.setDuty(duty);
            duty.setDutyIntervals(Set.of(dutyInterval));
            duties.add(duty);
        }

        action.setDemands(demands);

        // Mock the behavior of repositories
        when(actionRepository.findById(actionId)).thenReturn(Optional.of(action));
        when(dutyRepository.findAll()).thenReturn(duties);

        // Call the method to be tested
        ActionScheduleDto result = scheduleService.getScheduleByAction(actionId);

        // Verify the result
        assertNotNull(result);
        assertEquals(actionId, result.actionId());
        assertEquals("Test Action", result.heading());
        assertEquals("Test Description", result.description());

        List<DemandDto> demandDtos = result.demands();
        assertNotNull(demandDtos);
        assertEquals(3, demandDtos.size());

        for (int i = 0; i < 3; i++) {
            DemandDto demandDto = demandDtos.get(i);
            assertEquals((long) (i + 1), demandDto.demandId());
            assertEquals(LocalDate.of(2024, 7, 20).plusDays(i), demandDto.date());

            List<DemandIntervalDto> intervalDtos = demandDto.demandIntervals();
            assertNotNull(intervalDtos);
            assertEquals(1, intervalDtos.size());

            DemandIntervalDto intervalDto = intervalDtos.get(0);
            assertEquals((long) (i + 1), intervalDto.intervalId());
            assertEquals(LocalTime.of(9, 0), intervalDto.startTime());
            assertEquals(LocalTime.of(12, 0), intervalDto.endTime());

            List<VolunteerDto> assignedVolunteers = intervalDto.assignedVolunteers();
            assertNotNull(assignedVolunteers);
            assertEquals(1, assignedVolunteers.size());

            VolunteerDto volunteerDto = assignedVolunteers.get(0);
            assertEquals((long) (i + 1), volunteerDto.volunteerId());
            assertEquals("John" + i, volunteerDto.firstname());
            assertEquals("Doe" + i, volunteerDto.lastname());
        }

        // Verify the interactions with the repositories
        verify(actionRepository, times(1)).findById(actionId);
        verify(dutyRepository, times(3)).findAll();
    }


}
