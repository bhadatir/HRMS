package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    TravelPlan findTravelPlanByFkTravelPlanHREmployee_Id(Long fkTravelPlanHREmployeeId);

    TravelPlan findTravelPlanById(Long id);

    @Query(value = "select e.id " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee = :empId " +
            "and e.fkTravelPlan = :travelId")
    Long findEmployeeTravelPlanId(@Param("empId") Employee empId, @Param("travelId") TravelPlan travelId);

    @Query(value = "SELECT t.id " +
            "FROM TravelPlan t " +
            "WHERE lower(t.travelPlanName) like lower(concat(:query,'%')) " +
            "or lower(t.travelPlanFrom) like lower(concat(:query,'%')) " +
            "or lower(t.travelPlanTo) like lower(concat(:query,'%')) ")
    List<Long> findTravelPlan(String query);

    @Query(value = "select TravelPlan " +
            "from TravelPlan t " +
            "where t.travelPlanIsDeleted = :b " +
            "and t.travelPlanEndDate = :now")
    List<TravelPlan> findAllByTravelPlanEndDateAndTravelPlanIsDeleted(LocalDate now, boolean b);
}

