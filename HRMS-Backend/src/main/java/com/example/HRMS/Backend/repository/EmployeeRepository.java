package com.example.HRMS.Backend.repository;

import java.util.List;
import java.util.Optional;

import com.example.HRMS.Backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findEmployeeByEmployeeEmail(String employeeEmail);

    Employee findEmployeeById(Long id);

    boolean existsByEmployeeEmail(String employeeEmail);

    @Query(value = "WITH ManagerChain AS (" +
            "SELECT pk_employee_id, employee_first_name, employee_last_name, fk_manager_employee_id, 1 as level " +
            "FROM employee WHERE pk_employee_id = :empId " +
            "UNION ALL " +
            "SELECT e.pk_employee_id, e.employee_first_name, e.employee_last_name, e.fk_manager_employee_id, mc.level + 1 " +
            "FROM employee e INNER JOIN ManagerChain mc ON e.pk_employee_id = mc.fk_manager_employee_id) " +
            "SELECT * FROM ManagerChain ORDER BY level DESC", nativeQuery = true)
    List<Object[]> getUpwardManagerChainEntities(@Param("empId") Long empId);

    List<Employee> findByFkManagerEmployeeId(Long managerId);
}
