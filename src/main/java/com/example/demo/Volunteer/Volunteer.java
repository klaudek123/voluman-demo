package com.example.demo.Volunteer;

import com.example.demo.Action.Action;
import com.example.demo.Volunteer.Availability.Availability;
import com.example.demo.Volunteer.Duty.Duty;
import com.example.demo.Volunteer.Preferences.Preferences;
import com.example.demo.Volunteer.Role.VolunteerRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "volunteer")
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long volunteerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private VolunteerRole role;

    @Column(name = "limit_of_weekly_hours", nullable = false, length = 3)
    private double limitOfWeeklyHours;

    @Column(name = "current_weekly_hours", nullable = false, length = 3)
    private double currentWeeklyHours;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "volunteer_details_id", referencedColumnName = "volunteerId")
    private VolunteerDetails volunteerDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "preferences_id", referencedColumnName = "preferenceId")
    private Preferences preferences = new Preferences();

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Availability> availabilities = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "volunteer_action",
            joinColumns = @JoinColumn(name = "volunteer_id"),
            inverseJoinColumns = @JoinColumn(name = "action_id")
    )
    @JsonIgnore
    private Set<Action> actions = new HashSet<>();

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Duty> duties = new HashSet<>();


    public double calculateCurrentWeeklyHours(LocalDate startOfWeek, LocalDate endOfWeek) {
        return duties.stream()
                .filter(duty -> !duty.getDate().isBefore(startOfWeek) && !duty.getDate().isAfter(endOfWeek))
                .mapToDouble(Duty::getTotalDurationHours)
                .sum();
    }

    @PrePersist
    public void prePersist() {
        if (this.preferences == null) {
            this.preferences = new Preferences();
        }

        if (this.actions == null) {
            this.actions = new HashSet<>();
        }

        if (this.duties == null) {
            this.duties = new HashSet<>();
        }

        if (this.availabilities == null) {
            this.availabilities = new ArrayList<>();
        }
    }

}
