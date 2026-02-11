package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.ExpenseProofType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseProofTypeRepository extends JpaRepository<ExpenseProofType,Long> {

    ExpenseProofType findExpenseProofTypeById(Long id);
}