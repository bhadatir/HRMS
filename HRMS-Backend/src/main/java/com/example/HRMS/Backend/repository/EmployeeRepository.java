package com.example.HRMS.Backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.HRMS.Backend.dto.EmployeeSearch;
import com.example.HRMS.Backend.dto.ParticipantsSearch;
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

    @Query(value = "SELECT NEW com.example.HRMS.Backend.dto.EmployeeSearch( " +
            "e.id, e.employeeFirstName, e.employeeLastName, " +
            "e.employeeEmail, e.fkRole.id, e.fkRole.roleName, e.fkPosition.id, " +
            "e.fkPosition.positionName, e.fkDepartment.id, " +
            "e.fkDepartment.departmentName ) " +
            "FROM Employee e " +
            "WHERE lower(e.employeeFirstName) like lower(concat(:query,'%')) " +
            "or lower(e.employeeLastName) like lower(concat(:query,'%')) " +
            "or lower(e.employeeEmail) like lower(concat(:query,'%')) ")
    Page<EmployeeSearch> searchEmployeeByName(@Param("query") String query, Pageable pageable);

    @Query(value = """
            SELECT
             e.pk_employee_id, e.employee_first_name, e.employee_last_name,
             e.employee_email, e.fk_role_id, e.fk_position_id,
             e.fk_department_id
             FROM employee e
             join employee_game_interest egi ON egi.fk_employee_id = e.pk_employee_id
             WHERE lower(e.employee_first_name) like lower(concat(:query,'%'))
             or lower(e.employee_last_name) like lower(concat(:query,'%'))
             or lower(e.employee_email) like lower(concat(:query,'%'))
             AND egi.fk_game_type_id = :gameTypeId
             AND egi.is_interest_deleted = 0
             AND e.pk_employee_id NOT IN
             (
             SELECT b.fk_host_employee_id FROM game_booking b
             WHERE b.game_booking_is_deleted = 0 AND b.fk_game_booking_status_id = 1
             AND ( :start < b.game_booking_end_time AND :end > b.game_booking_start_time)
             UNION
             SELECT bp.fk_employee_id FROM game_booking b2
             join booking_participants bp on bp.fk_game_booking_id = b2.pk_game_booking_id
             WHERE b2.game_booking_is_deleted = 0 AND b2.fk_game_booking_status_id = 1
             AND ( :start < b2.game_booking_end_time AND :end > b2.game_booking_start_time)
             UNION
             SELECT w.fk_host_employee_id FROM booking_waiting_list w
             WHERE w.waiting_status_is_active = 1
             AND ( :start < w.target_slot_end_datetime AND :end > w.target_slot_datetime)
             UNION
             SELECT wp.fk_employee_id FROM booking_waiting_list w2
             join booking_participants wp on wp.fk_booking_waiting_list_id = w2.pk_waiting_id
             WHERE w2.waiting_status_is_active = 1
             AND ( :start < w2.target_slot_end_datetime AND :end > w2.target_slot_datetime)
             )
            """,
            nativeQuery = true)
    Page<ParticipantsSearch> searchAvailableParticipants1(@Param("query") String query,
                                                         @Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end,
                                                         @Param("gameTypeId") Long gameTypeId,
                                                         Pageable pageable );

    @Query(value =
            "SELECT NEW com.example.HRMS.Backend.dto.EmployeeSearch( " +
                    "e.id, e.employeeFirstName, e.employeeLastName, " +
                    "e.employeeEmail, e.fkRole.id, e.fkRole.roleName, e.fkPosition.id, " +
                    "e.fkPosition.positionName, e.fkDepartment.id, " +
                    "e.fkDepartment.departmentName ) " +
                    "FROM Employee e " +
                    "JOIN EmployeeGameInterest egi ON egi.fkEmployee.id = e.id " +
                    "WHERE (lower(e.employeeFirstName) like lower(concat(:query,'%')) " +
                    "or lower(e.employeeLastName) like lower(concat(:query,'%')) " +
                    "or lower(e.employeeEmail) like lower(concat(:query,'%'))) " +
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
                    ")")
    Page<EmployeeSearch> searchAvailableParticipants(
            @Param("query") String query,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("gameTypeId") Long gameTypeId,
            Pageable pageable
    );


}

