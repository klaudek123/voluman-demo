package com.example.demo.Schedule;

import com.example.demo.Action.Action;
import com.example.demo.Action.ActionDto.ActionDto;
import com.example.demo.Action.ActionDto.ActionScheduleDto;
import com.example.demo.Action.ActionRepository;
import com.example.demo.Action.ActionService;
import com.example.demo.Action.Demand.Demand;
import com.example.demo.Action.Demand.DemandDto;
import com.example.demo.Action.Demand.DemandInterval.DemandInterval;
import com.example.demo.Action.Demand.DemandInterval.DemandIntervalDto;
import com.example.demo.Action.Demand.DemandRepository;
import com.example.demo.Action.Demand.DemandService;
import com.example.demo.Schedule.ScheduleDto.*;
import com.example.demo.Volunteer.Availability.Availability;
import com.example.demo.Volunteer.Availability.AvailabilityInterval.AvailabilityInterval;
import com.example.demo.Volunteer.Availability.AvailabilityService;
import com.example.demo.Volunteer.Duty.Duty;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyInterval;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyIntervalDto;
import com.example.demo.Volunteer.Duty.DutyInterval.DutyIntervalStatus;
import com.example.demo.Volunteer.Duty.DutyRepository;
import com.example.demo.Volunteer.Duty.DutyService;
import com.example.demo.Volunteer.Preferences.Preferences;
import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Volunteer.VolunteerDto.VolunteerDto;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Volunteer.VolunteerRepository;
import com.example.demo.Volunteer.VolunteerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ActionService actionService;
    private final ActionRepository actionRepository;
    private final VolunteerService volunteerService;
    private final VolunteerRepository volunteerRepository;
    private final AvailabilityService availabilityService;
    private final DemandService demandService;
    private final DutyService dutyService;
    private final DutyRepository dutyRepository;
    private final DemandRepository demandRepository;

    public ScheduleService(ActionService actionService, ActionRepository actionRepository, VolunteerService volunteerService, VolunteerRepository volunteerRepository, AvailabilityService availabilityService, DemandService demandService, DutyService dutyService, DutyRepository dutyRepository, DutyRepository dutyRepository1, DemandRepository demandRepository) {
        this.actionService = actionService;
        this.actionRepository = actionRepository;
        this.volunteerService = volunteerService;
        this.volunteerRepository = volunteerRepository;
        this.availabilityService = availabilityService;
        this.demandService = demandService;
        this.dutyService = dutyService;
        this.dutyRepository = dutyRepository1;
        this.demandRepository = demandRepository;
    }


    public void choosePref(Long actionId, ActionPrefRequest actionPrefRequest) {
        switch (actionPrefRequest.decision()) {
            case "T":
                actionService.addDetermined(actionId, actionPrefRequest.volunteerId());
                actionService.addVolunteer(actionId, actionPrefRequest.volunteerId());
                volunteerService.addPreferences(actionId, actionPrefRequest.volunteerId(), Decision.T);
                break;
            case "R":
                actionService.addVolunteer(actionId, actionPrefRequest.volunteerId());
                volunteerService.addPreferences(actionId, actionPrefRequest.volunteerId(), Decision.R);
                break;
            case "N":
                volunteerService.addPreferences(actionId, actionPrefRequest.volunteerId(), Decision.N);
                break;
            default:
                throw new IllegalArgumentException("Invalid decision value: " + actionPrefRequest.decision());
        }

    }



    public void scheduleNeedAction(Long actionId, int year, int week, ActionNeedRequest actionNeedRequest) throws Exception {
        // Validate leader
        if (!volunteerRepository.existsByVolunteerIdAndRole(actionNeedRequest.getLeaderId(), VolunteerRole.LEADER)) {
            throw new Exception("Leader not found or not authorized.");
        }

        // Get action
        Optional<Action> actionOptional = actionRepository.findById(actionId);
        if (actionOptional.isEmpty()) {
            throw new Exception("Action not found.");
        }

        Action action = actionOptional.get();

        // Get start and end date of the week
        LocalDate startOfWeek = getStartOfWeek(year, week);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        for (ActionNeedRequest.DayRequest dayRequest : actionNeedRequest.getDays()) {
            LocalDate requestDate = dayRequest.getDate();

            // Validate date
            if (requestDate.isBefore(action.getStartDay()) || requestDate.isAfter(action.getEndDay())) {
                throw new Exception("Date " + requestDate + " is not within the action's duration.");
            }

            if (requestDate.isBefore(startOfWeek) || requestDate.isAfter(endOfWeek)) {
                throw new Exception("Date " + requestDate + " is not within the specified week.");
            }

            // Check if a demand already exists for the given date
            Optional<Demand> existingDemandOptional = demandService.findByActionAndDate(action, requestDate);
            Demand demand = existingDemandOptional.orElseGet(() -> {
                Demand newDemand = new Demand();
                newDemand.setAction(action);
                newDemand.setDate(requestDate);
                return newDemand;
            });

            // Modify existing intervals instead of replacing the collection
            Set<DemandInterval> existingIntervals = demand.getDemandIntervals();

            // Clear existing intervals to prepare for new ones
            existingIntervals.clear();

            // Loop over the slots provided in the request and either update or add intervals
            for (ActionNeedRequest.SlotRequest slotRequest : dayRequest.getSlots()) {
                LocalTime slotStart = slotRequest.getStartTime();
                LocalTime slotEnd = slotRequest.getEndTime();
                Long needMin = slotRequest.getNeedMin();
                Long needMax = slotRequest.getNeedMax();

                // Check if an interval with the same times and needs already exists
                boolean intervalExists = false;
                for (DemandInterval existingInterval : existingIntervals) {
                    if (existingInterval.getStartTime().equals(slotStart) &&
                            existingInterval.getEndTime().equals(slotEnd) &&
                            existingInterval.getNeedMin().equals(needMin) &&
                            existingInterval.getNeedMax().equals(needMax)) {
                        // Interval already exists, update it (optional depending on requirements)
                        existingInterval.setNeedMin(needMin);
                        existingInterval.setNeedMax(needMax);
                        intervalExists = true;
                        break;
                    }
                }

                // If the interval does not exist, create a new one
                if (!intervalExists) {
                    DemandInterval demandInterval = new DemandInterval();
                    demandInterval.setStartTime(slotStart);
                    demandInterval.setEndTime(slotEnd);
                    demandInterval.setNeedMin(needMin);
                    demandInterval.setNeedMax(needMax);
                    demandInterval.setDemand(demand);

                    existingIntervals.add(demandInterval);
                }
            }

            // Save or update demand
            demandService.addDemand(demand);

            // Add the demand to the action's demands if it wasn't already added
            if (!existingDemandOptional.isPresent()) {
                action.getDemands().add(demand);
            }
        }

        // Save the action with updated demands
        actionRepository.save(action);
    }







    public Optional<Action> getActionById(Long actionId) {
        return actionRepository.findById(actionId);
    }

    // Helper method to get the start of the week
    private LocalDate getStartOfWeek(int year, int week) {
        return LocalDate.ofYearDay(year, 1)
                .with(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek())
                .plusWeeks(week - 1);
    }

    public void chooseAvailabilities(Long volunteerId, int year, int week, VolunteerAvailRequest availRequest) throws Exception {
        // Validate volunteer
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new Exception("Volunteer not found"));

        // Validate and process availability for each day
        for (VolunteerAvailRequest.DayAvailabilityRequest dayRequest : availRequest.getDays()) {
            LocalDate requestDate = dayRequest.getDate();

            // Validate if the date belongs to the specified week
            int requestWeek = requestDate.get(WeekFields.ISO.weekOfWeekBasedYear());
            if (requestDate.getYear() != year || requestWeek != week) {
                throw new Exception("Date " + requestDate + " does not belong to week " + week + " of year " + year);
            }

            // Create or update availability for the volunteer for this date
            Availability availability = availabilityService.getByVolunteerIdAndDate(volunteerId, requestDate);

            if (availability.getAvailabilityId() == null) {
//                availability = new Availability();
                availability.setVolunteer(volunteer);
                availability.setDate(requestDate);
                volunteer.getAvailabilities().add(availability);
            }

            // Process slots for the day
            Set<AvailabilityInterval> availabilityIntervals = availability.getSlots();
            if (availabilityIntervals == null) {
                availabilityIntervals = new HashSet<>();
                availability.setSlots(availabilityIntervals);
            } else {
                availabilityIntervals.clear(); // Clear existing intervals if necessary
            }

            for (VolunteerAvailRequest.AvailabilitySlotRequest slotRequest : dayRequest.getSlots()) {
                AvailabilityInterval interval = new AvailabilityInterval();
                interval.setStartTime(slotRequest.getStartTime());
                interval.setEndTime(slotRequest.getEndTime());
                interval.setAvailability(availability);
                availabilityIntervals.add(interval);
            }

            availability.setSlots(availabilityIntervals);
            availabilityService.addAvail(availability);
            volunteer.getAvailabilities().add(availability);
        }

        // Update volunteer's limit of hours
        volunteer.setLimitOfWeeklyHours(availRequest.getLimitOfHours());
        volunteerRepository.save(volunteer);
    }

//    public Schedule showSchedule(int year, int week, int actionId) {
//        //
//
//        return ;
//    }

    public void generateSchedule(LocalDate date) {
        /*List<Availability> availabilities = availabilityService.getAvailabilitiesForDay(date);
         */

        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        System.out.println("Generating schedule for the week: " + startOfWeek + " to " + endOfWeek);

        // Iteracja po każdym dniu tygodnia
        for (LocalDate currentDay = startOfWeek; !currentDay.isAfter(endOfWeek); currentDay = currentDay.plusDays(1)) {
            System.out.println("Processing date: " + currentDay);
            resetCurrentVolunteersNumber(demandService.getDemandsForDay(currentDay));

            // Pobierz dostępności i zapotrzebowania dla danego dnia
            List<Availability> availabilities = availabilityService.getAvailabilitiesForDay(currentDay);
            List<Demand> demands = demandService.getDemandsForDay(currentDay);

            if (demands.isEmpty() || availabilities.isEmpty()) {
                System.out.println("No demands or availabilities for " + currentDay);
                continue;
            }

            for (Demand demand : demands) {
                // Pobierz listę zainteresowanych wolontariuszy
                Set<Volunteer> interestedVolunteers = getInterestedVolunteersForAction(demand.getAction().getActionId());

                // Przefiltruj dostępności do zainteresowanych wolontariuszy
                List<Availability> filteredAvailabilities = availabilities.stream()
                        .filter(availability -> interestedVolunteers.contains(availability.getVolunteer()))
                        .toList();

                for (DemandInterval demandInterval : demand.getDemandIntervals()) {
                    // Znajdź dostępności zgodne z interwałem zapotrzebowania
                    List<Availability> matchingAvailabilities = filteredAvailabilities.stream()
                            .filter(availability -> isAvailabilityMatchingInterval(availability, demandInterval))
                            .toList();

                    for (Availability matchingAvailability : matchingAvailabilities) {
                        Volunteer volunteer = matchingAvailability.getVolunteer();

                        // Sprawdź limit tygodniowy i maksymalne obciążenie
                        if (isWithinWeeklyLimit(volunteer, currentDay)) {
                            // Utwórz nowy interwał dyżuru dla wolontariusza
                            createDutyInterval(volunteer, demandInterval);

                            // Aktualizuj tygodniowe obciążenie wolontariusza
                            updateWeeklyLoad(volunteer, currentDay);

                            // Zaktualizuj liczbę wolontariuszy w interwale zapotrzebowania
                            demandInterval.setCurrentVolunteersNumber(demandInterval.getCurrentVolunteersNumber() + 1);

                            System.out.println("Assigned volunteer " + volunteer.getVolunteerId() +
                                    " to demand interval on " + currentDay);

                            // Przerwij, jeśli zapotrzebowanie jest już w pełni pokryte
                            if (demandInterval.getCurrentVolunteersNumber() >= demandInterval.getNeedMax()) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Zapisz aktualne obciążenie wolontariuszy i przelicz plany dyżurów
        //saveVolunteersWeeklyLoad();
        //recalculateDutyPlansForDay(date);
    }

    public Set<Volunteer> getInterestedVolunteersForAction(Long actionId) {
        Optional<Action> actionOpt = actionRepository.findById(actionId);

        if (actionOpt.isPresent()) {
            Action action = actionOpt.get();
            Set<Volunteer> interestedVolunteers = new HashSet<>();

            List<Volunteer> allVolunteers = volunteerRepository.findAll();
            for (Volunteer volunteer : allVolunteers) {
                Preferences preferences = volunteer.getPreferences();
                if (preferences != null) {
                    if (preferences.getT().contains(action) || preferences.getR().contains(action)) {
                        interestedVolunteers.add(volunteer);
                    }
                }
            }

            return interestedVolunteers;
        }

        return Collections.emptySet();
    }

    private boolean isAvailabilityMatchingInterval(Availability availability, DemandInterval demandInterval) {
        // Sprawdź czy interwał dostępności pokrywa się z interwałem zapotrzebowania
        return availability.getSlots().stream()
                .anyMatch(slot -> isSlotMatchingInterval(slot, demandInterval));
    }

    private boolean isSlotMatchingInterval(AvailabilityInterval slot, DemandInterval demandInterval) {
        // Sprawdź czy slot dostępności pokrywa się z interwałem zapotrzebowania
        return slot.getStartTime().equals(demandInterval.getStartTime()) && slot.getEndTime().equals(demandInterval.getEndTime());
    }

    private boolean isWithinWeeklyLimit(Volunteer volunteer, LocalDate date) {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        double currentWeeklyLoad = volunteer.calculateCurrentWeeklyHours(startOfWeek, endOfWeek);
        return currentWeeklyLoad <= volunteer.getLimitOfWeeklyHours();
    }


    private void createDutyInterval(Volunteer volunteer, DemandInterval demandInterval) {
        Duty duty = volunteer.getDuties().stream()
                .filter(d -> d.getDate().equals(demandInterval.getDemand().getDate()))
                .findFirst()
                .orElseGet(() -> {
                    Duty newDuty = new Duty();
                    newDuty.setVolunteer(volunteer);
                    newDuty.setDate(demandInterval.getDemand().getDate());
                    volunteer.getDuties().add(newDuty);
                    return newDuty;
                });


        // Sprawdź, czy istnieje już DutyInterval dla danego interwału
        Optional<DutyInterval> existingInterval = duty.getDutyIntervals().stream()
                .filter(interval -> interval.getStartTime().equals(demandInterval.getStartTime()) &&
                        interval.getEndTime().equals(demandInterval.getEndTime()))
                .findFirst();

        if (existingInterval.isPresent()) {
            // Jeśli istnieje, inkrementuj pole assign
            DutyInterval interval = existingInterval.get();
            dutyService.updateDutyInterval(interval); // Aktualizuj interwał w bazie danych
        } else {
            // Jeśli nie istnieje, utwórz nowy DutyInterval
            DutyInterval newInterval = new DutyInterval();
            newInterval.setStartTime(demandInterval.getStartTime());
            newInterval.setEndTime(demandInterval.getEndTime());
            newInterval.setStatus(DutyIntervalStatus.ASSIGNED);

            newInterval.setDuty(duty);
            dutyService.addDutyInterval(newInterval, duty); // Zapisz nowy interwał do bazy danych
        }
    }

    private void updateWeeklyLoad(Volunteer volunteer, LocalDate date) {
        // Ustalamy początek i koniec tygodnia
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Pobieramy wszystkie dyżury wolontariusza w danym tygodniu
        List<Duty> duties = volunteer.getDuties().stream()
                .filter(duty -> !duty.getDate().isBefore(startOfWeek) && !duty.getDate().isAfter(endOfWeek))
                .collect(Collectors.toList());

        // Sumujemy totalDurationHours z każdego Duty
        double totalWeeklyHours = duties.stream()
                .mapToDouble(Duty::getTotalDurationHours)  // zakładając, że masz metodę getTotalDurationHours w klasie Duty
                .sum();

        Volunteer updatedVolunteer = volunteerRepository.findById(volunteer.getVolunteerId())
                .orElseThrow(() -> new IllegalStateException("Volunteer not found after update"));
        // Ustawiamy obliczoną sumę jako aktualne obciążenie tygodniowe
        updatedVolunteer.setCurrentWeeklyHours(totalWeeklyHours);

        // Zapisz zmiany do bazy
        volunteerRepository.save(updatedVolunteer);

        System.out.println("Updated weekly load for volunteer " + volunteer.getVolunteerId() + ": " + volunteer.getCurrentWeeklyHours());
    }



    private void saveVolunteersWeeklyLoad() {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        for (Volunteer volunteer : volunteers) {
            double currentWeeklyLoad = volunteer.calculateCurrentWeeklyHours(startOfWeek, endOfWeek);
            volunteer.setCurrentWeeklyHours(currentWeeklyLoad);
            volunteerRepository.save(volunteer);
        }
    }

    private void resetCurrentVolunteersNumber(List<Demand> demands) {
        for (Demand demand : demands) {
            for (DemandInterval interval : demand.getDemandIntervals()) {
                interval.setCurrentVolunteersNumber(0L); // Resetowanie liczby wolontariuszy do 0
            }
        }
    }
    private void recalculateDutyPlansForDay(LocalDate date) {
        List<Duty> dutiesForDay = dutyService.findByDate(date);

        // Przelicz plany dyżurów dla danego dnia (np. generowanie raportów, statystyk, etc.)
        // Implementacja zależna od specyfiki Twojej aplikacji
    }

    public ActionScheduleDto getScheduleByAction(Long actionId) {
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action not found"));

        List<DemandDto> demandDtos = action.getDemands().stream()
                .map(demand -> {
                    List<DemandIntervalDto> intervalDtos = demand.getDemandIntervals().stream()
                            .map(demandInterval -> {
                                // Find matching DutyIntervals
                                List<VolunteerDto> assignedVolunteers = dutyRepository.findAll().stream()
                                        .flatMap(duty -> duty.getDutyIntervals().stream())
                                        .filter(dutyInterval ->
                                                dutyInterval.getStartTime().equals(demandInterval.getStartTime()) &&
                                                        dutyInterval.getEndTime().equals(demandInterval.getEndTime()) &&
                                                        dutyInterval.getDuty().getDate().equals(demandInterval.getDemand().getDate())
                                        )
                                        .map(dutyInterval -> {
                                            Volunteer volunteer = dutyInterval.getDuty().getVolunteer();
                                            return new VolunteerDto(
                                                    volunteer.getVolunteerId(),
                                                    volunteer.getVolunteerDetails().getFirstname(),
                                                    volunteer.getVolunteerDetails().getLastname()
                                            );
                                        })
                                        .collect(Collectors.toList());

                                return new DemandIntervalDto(
                                        demandInterval.getIntervalId(),
                                        demandInterval.getStartTime(),
                                        demandInterval.getEndTime(),
                                        assignedVolunteers
                                );
                            })
                            .collect(Collectors.toList());

                    return new DemandDto(
                            demand.getDemandId(),
                            demand.getDate(),
                            intervalDtos
                    );
                })
                .collect(Collectors.toList());

        return new ActionScheduleDto(
                action.getActionId(),
                action.getHeading(),
                action.getDescription(),
                demandDtos
        );
    }


    @Transactional
    public void modifySchedule(Long volunteerId, int year, int week, ModifyScheduleRequest modifyScheduleRequest) {
        // Pobranie wolontariusza
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new IllegalArgumentException("Volunteer not found"));

        year = validYear(year);
        week = validWeek(week);

        // Określenie początku i końca tygodnia
        LocalDate startOfWeek = LocalDate.ofYearDay(year, (week - 1) * 7 + 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Pobranie obowiązków wolontariusza z danego tygodnia
        List<Duty> duties = dutyRepository.findAllByVolunteerAndDateBetween(volunteer, startOfWeek, endOfWeek);

        // Pobranie wszystkich demand związanych z daną akcją
        List<Demand> demands = demandService.findAllByActionId(modifyScheduleRequest.actionId());

        // Przeliczenie czasu trwania interwałów, które mają być usunięte
        double totalCanceledHours = 0.0;

        // Iterowanie przez dutyIntervals z requestu
        for (DutyInterval requestInterval : modifyScheduleRequest.dutyIntervals()) {
            // Szukanie odpowiadającego interwału w istniejących obowiązkach
            for (Duty duty : duties) {
                for (DutyInterval interval : duty.getDutyIntervals()) {
                    if (interval.getIntervalId().equals(requestInterval.getIntervalId())
                            && interval.getStatus() == DutyIntervalStatus.ASSIGNED) {
                        interval.setStatus(DutyIntervalStatus.CANCELED);
                        totalCanceledHours += Duration.between(interval.getStartTime(), interval.getEndTime()).toMinutes() / 60.0;

                        // Znalezienie odpowiedniego DemandInterval i zmniejszenie currentVolunteersNumber
                        for (Demand demand : demands) {
                            Iterator<DemandInterval> iterator = demand.getDemandIntervals().iterator();
                            while (iterator.hasNext()) {
                                DemandInterval demandInterval = iterator.next();
                                if (demandInterval.getStartTime().equals(interval.getStartTime())
                                        && demandInterval.getEndTime().equals(interval.getEndTime())
                                        && interval.getDuty().getVolunteer().getActions().stream()
                                        .anyMatch(action -> action.equals(demandInterval.getDemand().getAction()))
                                ) {
                                    demandInterval.setCurrentVolunteersNumber(demandInterval.getCurrentVolunteersNumber() - 1);
                                    iterator.remove(); // Usunięcie z aktualnej kolekcji
                                }
                            }
                            demandRepository.save(demand);
                        }
                    }
                }
            }
        }

        // Zaktualizowanie currentWeeklyHours wolontariusza
        volunteer.setCurrentWeeklyHours(volunteer.getCurrentWeeklyHours() - totalCanceledHours);

        // Zapisanie zmian w obowiązkach
        dutyRepository.saveAll(duties);

        // Zapisanie zmian w wolontariuszu
        volunteerRepository.save(volunteer);
    }

    public VolunteerScheduleDto getScheduleByVolunteer(Long volunteerId, int year, int week) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new IllegalArgumentException("Volunteer not found"));

        year = validYear(year);
        week = validWeek(week);

        LocalDate startDate = getStartDateOfWeek(year, week);
        LocalDate endDate = startDate.plusDays(6);

        List<Duty> duties = dutyRepository.findByVolunteer_VolunteerIdAndDateBetween(volunteerId, startDate, endDate);
        List<Demand> demands = demandRepository.findByDateBetween(startDate, endDate);

        List<DutyIntervalDto> dutyIntervals = mapDutyIntervalsToDto(duties, demands);

        return new VolunteerScheduleDto(
                volunteer.getVolunteerId(),
                volunteer.getVolunteerDetails().getFirstname(),
                volunteer.getVolunteerDetails().getLastname(),
                dutyIntervals
        );
    }


    private List<DutyIntervalDto> mapDutyIntervalsToDto(List<Duty> duties, List<Demand> demands) {
        return duties.stream()
                .flatMap(duty -> duty.getDutyIntervals().stream())
                .flatMap(dutyInterval -> {
                    // Znajdź wszystkie odpowiadające demandInterval
                    List<DemandInterval> correspondingDemandIntervals = findCorrespondingDemandIntervals(demands, dutyInterval);

                    return correspondingDemandIntervals.stream().map(correspondingDemandInterval -> {
                        Action action = correspondingDemandInterval.getDemand().getAction();

                        return new DutyIntervalDto(
                                dutyInterval.getIntervalId(),
                                dutyInterval.getDuty().getDate(),
                                dutyInterval.getStartTime(),
                                dutyInterval.getEndTime(),
                                new ActionDto(action.getActionId(), action.getHeading())
                        );
                    });
                })
                .collect(Collectors.toList());
    }

    private List<DemandInterval> findCorrespondingDemandIntervals(List<Demand> demands, DutyInterval dutyInterval) {
        return demands.stream()
                .flatMap(demand -> demand.getDemandIntervals().stream())
                .filter(demandInterval ->
                        demandInterval.getStartTime().equals(dutyInterval.getStartTime()) &&
                                demandInterval.getEndTime().equals(dutyInterval.getEndTime()) &&
                                demandInterval.getDemand().getDate().equals(dutyInterval.getDuty().getDate())
                )
                .collect(Collectors.toList());
    }

    private int validWeek(int week) {
        if (week == 0) {
            week = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        }
        return week;
    }

    private int validYear(int year) {
        if (year == 0) {
            year = LocalDate.now().getYear();
        }
        return year;
    }

    private LocalDate getStartDateOfWeek(int year, int week) {
        return LocalDate.ofYearDay(year, 1).with(WeekFields.of(Locale.getDefault()).weekOfYear(), week);
    }
}
