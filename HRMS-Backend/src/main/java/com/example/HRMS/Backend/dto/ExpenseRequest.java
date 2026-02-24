package com.example.HRMS.Backend.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @Min(value = 0,message = "expense amount cannot be negative")
    private Integer expenseAmount;

    @NotNull(message = "expense date is required")
    private LocalDate expenseDate;

    @Size(max = 255)
    private String expenseRemark;

    @NotNull(message = "expense status is required")
    private Long fkExpenseExpenseStatusId;

    @NotNull(message = "Employee travel plan id is required")
    private Long fkEmployeeTravelPlanId;

    @Column(name = "reason_for_reject_expense")
    private String reasonForRejectExpense;
}
