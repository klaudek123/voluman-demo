package com.example.demo.Action;

import com.example.demo.Volunteer.VolunteerDto.LeaderDto;
import com.example.demo.Volunteer.Volunteer;
import com.example.demo.Action.Demand.Demand;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "action")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "heading", nullable = false, length = 50)
    private String heading;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", nullable = false)
    private ActionStatus status;

    @Column(name = "start_day", nullable = false)
    private LocalDate startDay;

    @Column(name = "end_day", nullable = false)
    private LocalDate endDay;

    @Embedded
    private LeaderDto leader;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Demand> demands;

    @ManyToMany(mappedBy = "actions", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    private Set<Volunteer> volunteers = new HashSet<>(); //T/R

    @ManyToMany(mappedBy = "actions", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    private Set<Volunteer> determined = new HashSet<>(); //T


}

/*
//    @ManyToMany(mappedBy = "T", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<Preferences> preferencesT = new HashSet<>();
//
//    @ManyToMany(mappedBy = "R", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<Preferences> preferencesR = new HashSet<>();
//
//    @ManyToMany(mappedBy = "N", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<Preferences> preferencesN = new HashSet<>();
 */