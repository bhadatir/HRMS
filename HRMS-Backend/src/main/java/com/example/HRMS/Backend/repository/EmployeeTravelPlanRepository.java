package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeTravelPlanRepository extends JpaRepository<EmployeeTravelPlan, Long> {
    EmployeeTravelPlan findEmployeeTravelPlanById(Long id);

    List<EmployeeTravelPlan> findEmployeeTravelPlanByFkTravelPlan_Id(Long fkTravelPlanId);

    @Query(value = "select e.id " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee.id = :empId " +
            "and e.fkTravelPlan.id = :travelPlanId ")
    Long findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(@Param("empId") Long empId, @Param("travelPlanId") Long travelPlanId);

}
