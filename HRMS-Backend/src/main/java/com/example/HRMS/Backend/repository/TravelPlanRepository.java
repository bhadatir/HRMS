package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelPlan;
import com.example.HRMS.Backend.model.TravelPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    TravelPlan findTravelPlanByFkTravelPlanHREmployee_Id(Long fkTravelPlanHREmployeeId);

    TravelPlan findTravelPlanById(Long id);

    @Query(value = "select e.id " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee = :empId " +
            "and e.fkTravelPlan = :travelId")
    Long findEmployeeTravelPlanId(@Param("empId") Employee empId, @Param("travelId") TravelPlan travelId);
}

