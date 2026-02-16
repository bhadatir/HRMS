package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.dto.ExpenseProofResponse;
import com.example.HRMS.Backend.model.ExpenseProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseProofRepository extends JpaRepository<ExpenseProof,Long> {

    List<ExpenseProof> findExpenseProofByFkExpense_Id(Long fkExpenseId);
}
