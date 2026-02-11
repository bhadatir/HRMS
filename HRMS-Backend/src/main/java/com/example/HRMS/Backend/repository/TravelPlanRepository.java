package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.TravelPlan;
import com.example.HRMS.Backend.model.TravelPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    TravelPlan findTravelPlanByFkTravelPlanHREmployee_Id(Long fkTravelPlanHREmployeeId);

    TravelPlan findTravelPlanById(Long id);

}

