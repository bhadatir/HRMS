package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.model.TravelPlan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelDocRepository extends JpaRepository<TravelDoc, Long> {
    List<TravelDoc> findTravelDocByFkEmployee_Id(Long fkEmployeeId);

    @Modifying
    @Transactional
    @Query("UPDATE TravelDoc td " +
            "SET td.docIsDeletedFromTravel = true " +
            "WHERE td.fkEmployee = :fkEmployee " +
            "and td.fkTravelPlan = :fkTravelPlan " )
    void findByFkEmployeeAndFkTravelPlanAndRemoveIt(Employee fkEmployee, TravelPlan fkTravelPlan);

    List<TravelDoc> findTravelDocsByFkEmployeeTravelPlan_Id(Long employeeTravelPlanId);

    List<TravelDoc> findByFkTravelPlan(TravelPlan fkTravelPlan);

    @Modifying
    @Transactional
    @Query("UPDATE TravelDoc td " +
            "SET td.docIsDeletedFromTravel = true " +
            "WHERE td.fkEmployee = :fkEmployee " +
            "and td.fkEmployeeTravelPlan = :fkEmployeeTravelPlan " )
    void findByFkEmployeeAndFkEmployeeTravelPlanAndRemoveIt(Employee fkEmployee, EmployeeTravelPlan fkEmployeeTravelPlan);

    List<TravelDoc> findByFkEmployeeAndFkTravelPlan(Employee fkEmployee, TravelPlan fkTravelPlan);

    List<TravelDoc> findTravelDocsByFkTravelPlan(TravelPlan fkTravelPlan);
}
