package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Expense;
import com.example.HRMS.Backend.model.ExpenseProof;
import com.example.HRMS.Backend.model.TravelPlanStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {

    @NotNull(message = "expense id is required") Expense findExpensesById(Long id);
}

