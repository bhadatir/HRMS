package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {

    Expense findExpensesById(Long id);

    @Query("SELECT COALESCE(SUM(e.expenseAmount), 0) FROM Expense e " +
            "WHERE e.fkEmployeeTravelPlan.fkTravelPlan.id = :planId " +
            "AND e.fkEmployeeTravelPlan.fkEmployee.id = :empId " +
            "AND e.expenseDate = :date " +
            "AND e.fkExpenseExpenseStatus.id != 3")
    Integer getTotalSpentByDate(Long planId, Long empId, LocalDate date);

    List<Expense> findExpenseByFkEmployeeTravelPlan(EmployeeTravelPlan fkEmployeeTravelPlan);
}

