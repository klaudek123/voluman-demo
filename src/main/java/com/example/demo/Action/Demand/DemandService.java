package com.example.demo.Action.Demand;

import com.example.demo.Action.Action;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DemandService {
    private final DemandRepository demandRepository;

    public DemandService(DemandRepository demandRepository) {
        this.demandRepository = demandRepository;
    }

    public Optional<Demand> findByActionAndDate(Action action, LocalDate date) {
        return demandRepository.findByActionAndDate(action, date);
    }

    public void addDemand(Demand demand) {
        demandRepository.save(demand);
    }

    public List<Demand> getDemandsForDay(LocalDate date) {
        return demandRepository.findAllByDate(date);
    }

    public List<Demand> findAllByActionId(Long actionId) {
        return demandRepository.findAllByAction_ActionId(actionId);
    }
}
