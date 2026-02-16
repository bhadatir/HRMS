package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.Expense;
import com.example.HRMS.Backend.model.ExpenseProof;
import com.example.HRMS.Backend.model.TravelPlanStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {

    Expense findExpensesById(Long id);

    List<Expense> findExpenseByFkEmployeeTravelPlan(EmployeeTravelPlan fkEmployeeTravelPlan);
}

