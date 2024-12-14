package com.example.demo.Action.Demand;

import com.example.demo.Action.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandRepository extends JpaRepository<Demand, Long> {

    List<Demand> findAllByDate(LocalDate date);

    List<Demand> findAllByAction_ActionId(Long actionId);

    Optional<Demand> findByActionAndDate(Action action, LocalDate date);

    List<Demand> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
