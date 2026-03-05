package com.example.HRMS.Backend.dto;

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
