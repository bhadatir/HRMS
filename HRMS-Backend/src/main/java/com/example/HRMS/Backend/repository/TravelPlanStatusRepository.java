package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.TravelPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelPlanStatusRepository extends JpaRepository<TravelPlanStatus, Long> {
    TravelPlanStatus findTravelPlanStatusById(Long id);
}
