package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseResponse {

    private Long id;

    private Integer expenseAmount;

    private LocalDate expenseDate;

    private String expenseRemark;

    private Instant expenseUploadedAt;

    private Long expenseExpenseStatusId;
    private String expenseExpenseStatusName;

    private long employeeTravelPlanId;

    private List<ExpenseProofResponse> expenseProofResponses;

    private List<TravelDocResponse> travelDocResponses;
}
