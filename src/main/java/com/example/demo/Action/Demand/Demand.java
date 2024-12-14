package com.example.demo.Action.Demand;

import com.example.demo.Action.Demand.DemandInterval.DemandInterval;
import com.example.demo.Action.Action;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "demand")
public class Demand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "demand_id")
    private Long demandId;

    @Column(name = "date",nullable = false)
    private LocalDate date;


    @ManyToOne
    @JoinColumn(name = "action_id")
    @JsonBackReference
    private Action action;


    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandInterval> demandIntervals = new HashSet<>();

}