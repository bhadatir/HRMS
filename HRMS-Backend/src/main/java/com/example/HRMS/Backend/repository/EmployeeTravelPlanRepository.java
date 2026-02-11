package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeTravelPlanRepository extends JpaRepository<EmployeeTravelPlan, Long> {
    EmployeeTravelPlan findEmployeeTravelPlanById(Long id);
}
