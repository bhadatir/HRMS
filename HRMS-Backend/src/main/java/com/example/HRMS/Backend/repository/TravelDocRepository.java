package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.model.TravelPlan;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Modifying
    @Transactional
    @Query("UPDATE TravelDoc td " +
            "SET td.docIsDeletedFromTravel = true " +
            "WHERE td.fkEmployee = :fkEmployee " +
            "and td.fkEmployeeTravelPlan = :fkEmployeeTravelPlan " )
    void findByFkEmployeeAndFkEmployeeTravelPlanAndRemoveIt(Employee fkEmployee, EmployeeTravelPlan fkEmployeeTravelPlan);

    List<TravelDoc> findByFkEmployeeAndFkTravelPlan(Employee fkEmployee, TravelPlan fkTravelPlan);

    @Query("SELECT DISTINCT td FROM TravelDoc td " +
            "LEFT JOIN td.fkEmployeeTravelPlan etp " +
            "WHERE (td.fkTravelPlan.id = :fkTravelPlanId OR etp.fkTravelPlan.id = :fkTravelPlanId) " +
            "AND (" +
            "   :roleId = 3 " +
            "   OR (:roleId = 2 AND (td.fkEmployee.id = :empId OR td.fkEmployee.fkManagerEmployee.id = :empId)) " +
            "   OR (:roleId = 1 AND EXISTS ( " +
            "       SELECT 1 FROM EmployeeTravelPlan memberCheck " +
            "       WHERE memberCheck.fkTravelPlan.id = :fkTravelPlanId " +
            "       AND memberCheck.fkEmployee.id = :empId " +
            "       AND memberCheck.employeeIsDeletedFromTravel = false" +
            "   ))" +
            ") " +
            "AND (lower(td.fkEmployee.employeeEmail) LIKE lower(concat(:searchTerm, '%')) " +
            "OR lower(td.fkTravelDocsType.travelDocsTypeName) LIKE lower(concat(:searchTerm, '%')) " +
            "OR CAST(td.travelDocUploadedAt AS string) LIKE concat(:searchTerm, '%')) " +
            "ORDER BY td.travelDocUploadedAt DESC")
    List<TravelDoc> findTravelDocs(
            @Param("fkTravelPlanId") Long fkTravelPlanId,
            @Param("empId") Long empId,
            @Param("roleId") Long roleId,
            @Param("searchTerm") String searchTerm
    );

    @Modifying
    @Transactional
    @Query("UPDATE TravelDoc td " +
            "SET td.docIsDeletedFromTravel = false " +
            "WHERE td.fkEmployee = :fkEmployee " +
            "and td.fkTravelPlan = :fkTravelPlan " )
    void findByFkEmployeeAndFkTravelPlanAndReAddIt(Employee fkEmployee, TravelPlan fkTravelPlan);

    @Modifying
    @Transactional
    @Query("UPDATE TravelDoc td " +
            "SET td.docIsDeletedFromTravel = false " +
            "WHERE td.fkEmployee = :fkEmployee " +
            "and td.fkEmployeeTravelPlan = :fkEmployeeTravelPlan " )
    void findByFkEmployeeAndFkEmployeeTravelPlanAndReAddIt(Employee fkEmployee, EmployeeTravelPlan fkEmployeeTravelPlan);
}
