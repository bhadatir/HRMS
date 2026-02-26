package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTravelPlanRepository extends JpaRepository<EmployeeTravelPlan, Long> {
    EmployeeTravelPlan findEmployeeTravelPlanById(Long id);

    List<EmployeeTravelPlan> findEmployeeTravelPlanByFkTravelPlan_Id(Long fkTravelPlanId);

    @Query(value = "select e.id " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee.id = :empId " +
            "and e.fkTravelPlan.id = :travelPlanId ")
    Long findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(@Param("empId") Long empId, @Param("travelPlanId") Long travelPlanId);

    @Query(value = "select e.fkEmployee.id " +
            "from EmployeeTravelPlan e " +
            "where e.fkTravelPlan.id = :travelPlanId " +
            "and e.employeeIsDeletedFromTravel = false ")
    List<Long> findEmployeeIdByTravelPlanId(@Param("travelPlanId") Long travelPlanId);

    void deleteEmployeeTravelPlanByFkEmployeeAndFkTravelPlan(Employee fkEmployee, TravelPlan fkTravelPlan);

    @Modifying
    @Query("UPDATE EmployeeTravelPlan e " +
            "SET e.employeeIsDeletedFromTravel = :isDeleted " +
            "WHERE e.fkEmployee.id IN :empIds " +
            "AND e.fkTravelPlan.id = :travelPlanId")
    void updateBatchStatus(List<Long> empIds, Long travelPlanId, boolean isDeleted);

    @Query(value = "select e.id " +
            "from EmployeeTravelPlan e " +
            "where e.fkTravelPlan.id = :travelPlanId " +
            "and e.employeeIsDeletedFromTravel = false ")
    List<Long> findEmployeeTravelPlanIdByTravelPlanId(Long travelPlanId);

    @Query(value = "select EmployeeTravelPlan " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee.id = :id " +
            "and e.fkTravelPlan = :id1 ")
    EmployeeTravelPlan findByFkEmployeeIdAndFkTravelPlanId(Long id, Long id1);


    @Query(value = "select count(e) > 0 " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee.id = :empId " +
            "and e.fkTravelPlan.id = :travelPlanId ")
    boolean isEmployeeTravelPlanByEmployeeIdAndTravelPlanIdExist(@Param("empId") Long empId, @Param("travelPlanId") Long travelPlanId);

}
