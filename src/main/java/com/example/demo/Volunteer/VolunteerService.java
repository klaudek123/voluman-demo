package com.example.demo.Volunteer;

import com.example.demo.Volunteer.Candidate.Candidate;
import com.example.demo.Volunteer.Preferences.Preferences;
import com.example.demo.Volunteer.Preferences.PreferencesService;
import com.example.demo.Schedule.Decision;
import com.example.demo.Action.Action;
import com.example.demo.Action.ActionService;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.example.demo.Volunteer.Role.RoleService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final ActionService actionService;
    private final PreferencesService preferencesService;
    private final RoleService roleService;

    public VolunteerService(VolunteerRepository volunteerRepository, ActionService actionService, PreferencesService preferencesService, RoleService roleService) {
        this.volunteerRepository = volunteerRepository;
        this.actionService = actionService;
        this.preferencesService = preferencesService;
        this.roleService = roleService;
    }

    public Volunteer addVolunteerFromCandidate(Optional<Candidate> candidate) {
        if (candidate.isPresent()) {
            VolunteerDetails volunteerDetails = mapCandidateToVolunteerDetails(candidate.get());

            Volunteer volunteer = new Volunteer();
            volunteer.setVolunteerDetails(volunteerDetails);
            volunteer.setRole(VolunteerRole.CANDIDATE);
            roleService.assignRole(volunteer, VolunteerRole.VOLUNTEER);
            volunteer.setLimitOfWeeklyHours(0L);
            volunteer.setCurrentWeeklyHours(0L);


            volunteerRepository.save(volunteer);
            return volunteer;
        }
        return null;
    }


    private VolunteerDetails mapCandidateToVolunteerDetails(Candidate candidate) {
        VolunteerDetails volunteerDetails = new VolunteerDetails();
        volunteerDetails.setFirstname(candidate.getFirstname());
        volunteerDetails.setLastname(candidate.getLastname());
        volunteerDetails.setEmail(candidate.getEmail());
        volunteerDetails.setPhone(candidate.getPhone());
        volunteerDetails.setDateOfBirth(candidate.getDateOfBirth());
        volunteerDetails.setCity(candidate.getCity());
        volunteerDetails.setStreet(candidate.getStreet());
        volunteerDetails.setHouseNumber(candidate.getHouseNumber());
        volunteerDetails.setApartmentNumber(candidate.getApartmentNumber());
        volunteerDetails.setPostalNumber(candidate.getPostalNumber());
        volunteerDetails.setSex(candidate.getSex());
        return volunteerDetails;
    }

    public void addPreferences(Long actionId, Long volunteerId, Decision decision) {
        Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);

        if (volunteer.isPresent()) {
            Volunteer volunteerEntity = volunteer.get();
            Optional<Action> action = actionService.getActionById(actionId);
            if (action.isPresent()) {
                Action actionEntity = action.get();
                Preferences preferences = volunteerEntity.getPreferences();
                if (preferences == null) {
                    preferences = new Preferences();
                    volunteerEntity.setPreferences(preferences);
                    preferencesService.addPreferences(preferences); // Zapisz nowe preferencje
                }

                if (decision == Decision.T) {
                    preferencesService.removeActionFromOtherPreferences(actionEntity, preferences.getPreferenceId(), Decision.T);
                    preferences.getT().add(actionEntity);
                }
                if (decision == Decision.R) {
                    preferencesService.removeActionFromOtherPreferences(actionEntity, preferences.getPreferenceId(), Decision.R);
                    preferences.getR().add(actionEntity);
                }
                if (decision == Decision.N) {
                    preferencesService.removeActionFromOtherPreferences(actionEntity, preferences.getPreferenceId(), Decision.N);
                    preferences.getN().add(actionEntity);
                }

                preferencesService.addPreferences(preferences);
                volunteerRepository.save(volunteerEntity);
            }
        }
    }


    public Volunteer addVolunteer(Volunteer volunteer) {
        roleService.assignRole(volunteer, VolunteerRole.VOLUNTEER);
        return volunteerRepository.save(volunteer);
    }

    public boolean isLeaderExist(Long leaderId) {
        return volunteerRepository.existsById(leaderId);
    }

    public Volunteer findVolunteerById(Long volunteerId) {
        return volunteerRepository.findById(volunteerId).get();
    }

    public boolean existsVolunteerByEmail(String email) {
        return volunteerRepository.existsByVolunteerDetailsEmail(email);
    }
}
