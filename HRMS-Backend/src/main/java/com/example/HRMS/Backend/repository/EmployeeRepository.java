package com.example.HRMS.Backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.HRMS.Backend.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findEmployeeByEmployeeEmail(String employeeEmail);

    Employee findEmployeeById(Long id);

    boolean existsByEmployeeEmail(String employeeEmail);

    //for OrgChart
    @Query(value = "WITH ManagerChain AS (" +
            "SELECT pk_employee_id, employee_first_name, employee_last_name, fk_manager_employee_id, 1 as level " +
            "FROM employee WHERE pk_employee_id = :empId " +
            "UNION ALL " +
            "SELECT e.pk_employee_id, e.employee_first_name, e.employee_last_name, e.fk_manager_employee_id, mc.level + 1 " +
            "FROM employee e INNER JOIN ManagerChain mc ON e.pk_employee_id = mc.fk_manager_employee_id) " +
            "SELECT * FROM ManagerChain ORDER BY level DESC", nativeQuery = true)
    List<Object[]> getUpwardManagerChainEntities(@Param("empId") Long empId);

    List<Employee> findByFkManagerEmployeeId(Long managerId);

    //for pass reset
    Optional<Employee> findByResetToken(String resetToken);

    @Query(value = "SELECT e " +
            "FROM Employee e " +
            "WHERE ((:employeeType = 0) " +
            "OR (:employeeType = 1 AND e.employeeIsActive = true) " +
            "OR (:employeeType = 2 AND e.employeeIsActive = false) " +
            ") " +
            "AND (lower(e.employeeFirstName) like lower(concat('%', :query,'%')) " +
            "or lower(e.employeeLastName) like lower(concat('%', :query,'%')) " +
            "or lower(e.employeeEmail) like lower(concat('%', :query,'%')) " +
            "OR CAST(e.employeeDob AS string) LIKE concat('%', :query, '%') " +
            "OR CAST(e.employeeHireDate AS string) LIKE concat('%', :query, '%') " +
            "OR CAST(e.employeeSalary AS string) LIKE concat('%', :query, '%') " +
            "or lower(e.employeeGender) like lower(concat('%', :query,'%')) " +
            "or lower(e.fkDepartment.departmentName) like lower(concat('%', :query,'%')) " +
            "or lower(e.fkPosition.positionName) like lower(concat('%', :query,'%')) " +
            "or lower(e.fkRole.roleName) like lower(concat('%', :query,'%'))) ")
    Page<Employee> searchEmployeeByName(@Param("query") String query, Long employeeType, Pageable pageable);

    @Query(value =
            "SELECT e " +
                    "FROM Employee e " +
                    "JOIN EmployeeGameInterest egi ON egi.fkEmployee.id = e.id " +
                    "WHERE (lower(e.employeeFirstName) like lower(concat('%', :query,'%')) " +
                    "or lower(e.employeeLastName) like lower(concat('%', :query,'%')) " +
                    "or lower(e.employeeEmail) like lower(concat('%', :query,'%'))) " +
                    "AND egi.fkGameType.id = :gameTypeId " +
                    "AND egi.isInterestDeleted = false " +
                    "AND NOT EXISTS ( " +
                    "    SELECT b FROM GameBooking b " +
                    "    LEFT JOIN BookingParticipant bp ON bp.fkGameBooking.id = b.id " +
                        "    WHERE b.gameBookingIsDeleted = false AND b.fkGameBookingStatus.id = 1 " +
                    "    AND (:start < b.gameBookingEndTime AND :end > b.gameBookingStartTime) " +
                    "    AND (b.fkHostEmployee.id = e.id OR bp.fkEmployee.id = e.id) " +
                    ") " +
                    "AND NOT EXISTS ( " +
                    "    SELECT w FROM BookingWaitingList w " +
                    "    LEFT JOIN BookingParticipant wp ON wp.fkBookingWaitingList.id = w.id " +
                    "    WHERE w.waitingStatusIsActive = true " +
                    "    AND (:start < w.targetSlotEndDatetime AND :end > w.targetSlotDatetime) " +
                    "    AND (w.fkHostEmployee.id = e.id OR wp.fkEmployee.id = e.id) " +
                    ")" +
                    "AND NOT EXISTS ( " +
                    "    SELECT etp FROM EmployeeTravelPlan etp " +
                    "    WHERE etp.fkTravelPlan.travelPlanIsDeleted = false " +
                    "    AND etp.employeeIsDeletedFromTravel = false " +
                    "    AND (etp.fkEmployee.id = e.id " +
                    "    OR etp.fkTravelPlan.fkTravelPlanHREmployee.id = e.id) " +
                    "    AND (CAST(:start AS LocalDate) <= etp.fkTravelPlan.travelPlanEndDate " +
                    "    AND CAST(:end AS LocalDate) >= etp.fkTravelPlan.travelPlanStartDate) " +
                    ")")
    Page<Employee> searchAvailableParticipants(
            @Param("query") String query,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("gameTypeId") Long gameTypeId,
            Pageable pageable
    );

    @Query(value = "SELECT e " +
            "FROM Employee e " +
            "WHERE lower(e.employeeFirstName) like lower(concat('%', :searchTerm,'%')) " +
            "or lower(e.employeeLastName) like lower(concat('%', :searchTerm,'%')) " +
            "or lower(e.fkRole.roleName) like lower(concat('%', :searchTerm,'%')) " +
            "or lower(e.fkPosition.positionName) like lower(concat('%', :searchTerm,'%')) " +
            "or lower(e.fkDepartment.departmentName) like lower(concat('%', :searchTerm,'%')) " +
            "or lower(e.employeeEmail) like lower(concat('%', :searchTerm,'%')) ")
    Page<Employee> findAllBySearchTerm(String searchTerm, Pageable pageable);
}

