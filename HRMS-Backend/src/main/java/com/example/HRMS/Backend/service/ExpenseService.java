package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.model.Expense;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExpenseService {
    void saveExpense(ExpenseRequest expenseRequest);

    void saveExpenseProof(Long proofTypeId, Long expenseId, MultipartFile file) throws IOException;
}
