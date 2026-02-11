package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.ExpenseProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseProofRepository extends JpaRepository<ExpenseProof,Long> {

}
