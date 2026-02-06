package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_expense_id", nullable = false)
    private Integer id;

    @Min(value = 0,message = "expense amount cannot be negative")
    @NotNull(message = "expense amount is required")
    @Column(name = "expense_amount", nullable = false)
    private Integer expenseAmount;

    @NotNull(message = "expense date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Size(max = 255)
    @Column(name = "expense_remark")
    private String expenseRemark;

    @ColumnDefault("getdate()")
    @Column(name = "expense_uploaded_at")
    private Instant expenseUploadedAt;

    @NotNull(message = "expense status is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_expense_travel_plan_status_id", nullable = false)
    private TravelPlanStatus fkExpenseTravelPlanStatus;

    @NotNull(message = "Employee travel plan id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_employee_travel_plan_id", nullable = false)
    private EmployeeTravelPlan fkEmployeeTravelPlan;


}