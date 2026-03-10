package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.TravelPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = "select count(e) > 0 " +
            "from EmployeeTravelPlan e " +
            "where e.fkEmployee.fkManagerEmployee.id = :empId " +
            "and e.fkTravelPlan.id = :travelPlanId ")
    boolean isEmployeeTravelPlanByManagerEmployeeIdAndTravelPlanIdExist(@Param("empId") Long empId, @Param("travelPlanId") Long travelPlanId);

    @Query("SELECT DISTINCT tp FROM TravelPlan tp " +
            "LEFT JOIN EmployeeTravelPlan etp ON etp.fkTravelPlan.id = tp.id " +
            "WHERE (tp.fkTravelPlanHREmployee.id = :empId " +
            "OR etp.fkEmployee.id = :empId " +
            "OR etp.fkEmployee.fkManagerEmployee.id = :empId) " +
            "AND (" +
            "  (:travelPlanType = 0) OR " +
            "  (:travelPlanType = 1 AND tp.travelPlanIsDeleted = false) OR " +
            "  (:travelPlanType = 2 AND tp.travelPlanIsDeleted = true) OR " +
            "  (:travelPlanType = 3 AND etp.employeeIsDeletedFromTravel = true AND etp.fkEmployee.id = :empId) OR " +
            "  (:travelPlanType = 4 AND tp.travelPlanIsReturn = true) OR " +
            "  (:travelPlanType = 5 AND tp.travelPlanIsReturn = false)" +
            ") " +
            "AND (CAST(tp.id AS string) LIKE concat('%', :searchTerm, '%') " +
            "OR lower(tp.travelPlanName) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR lower(tp.travelPlanDetails) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR lower(tp.travelPlanFrom) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR lower(tp.travelPlanTo) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR CAST(tp.travelPlanStartDate AS string) LIKE concat('%', :searchTerm, '%') " +
            "OR CAST(tp.travelPlanEndDate AS string) LIKE concat('%', :searchTerm, '%')) " +
            "ORDER BY tp.travelPlanCreatedAt DESC")
    Page<TravelPlan> findTravelPlanByFkEmployee_Id(Long empId, String searchTerm, Long travelPlanType, Pageable pageable);
}
