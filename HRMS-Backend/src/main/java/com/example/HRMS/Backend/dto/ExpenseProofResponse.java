package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Expense;
import com.example.HRMS.Backend.model.ExpenseProofType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class ExpenseProofResponse {
    private Long id;

    private Instant expenseProofUploadedAt;

    private String expenseProofUrl;

    private Long expenseId;

    private Long expenseProofTypeId;

    private String expenseProofTypeName;
}
