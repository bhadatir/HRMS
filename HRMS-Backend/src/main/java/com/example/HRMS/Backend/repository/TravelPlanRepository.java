package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelPlan;
import jakarta.validation.constraints.NotNull;
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

    @Query("""
        SELECT CASE WHEN COUNT(tp) > 0 THEN TRUE ELSE FALSE END
        FROM EmployeeTravelPlan emp
        JOIN emp.fkTravelPlan tp
        WHERE emp.fkEmployee.id = :id
          AND tp.travelPlanStartDate <= :endDate
          AND tp.travelPlanEndDate >= :startDate
          AND tp.travelPlanIsDeleted = false
          AND emp.employeeIsDeletedFromTravel = false
    """)
    boolean findAllByGameBookingStartTimeBetween(Long id,  LocalDate startDate, LocalDate endDate);
}

