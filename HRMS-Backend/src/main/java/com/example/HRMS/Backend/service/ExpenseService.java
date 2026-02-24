package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.ExpenseProofResponse;
import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.dto.ExpenseResponse;
import com.example.HRMS.Backend.model.Expense;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ExpenseService {
    void saveExpense(ExpenseRequest expenseRequest);

    void saveExpenseProof(Long proofTypeId, Long expenseId, MultipartFile file) throws IOException;

    void saveExpenseWithProof(@Valid ExpenseRequest expenseRequest, List<MultipartFile> files, List<Long> proofTypeId) throws IOException;

    void updateExpenseStatus(Long expId, Long statusId, String reason);

    List<ExpenseResponse> getExpenseById(Long employeeId, Long travelPlanId);

    List<ExpenseProofResponse> getExpenseProofById(Long expenseId);
}
