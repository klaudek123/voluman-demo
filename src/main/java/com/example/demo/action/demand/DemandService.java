package com.example.demo.action.demand;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DemandService {
    private final DemandRepository demandRepository;

    public DemandService(DemandRepository demandRepository) {
        this.demandRepository = demandRepository;
    }


    public void addDemand(Demand demand) {
        demandRepository.save(demand);
    }

    public List<Demand> getDemandsForDay(LocalDate date) {
        return demandRepository.findAllByDate(date);
    }
}
