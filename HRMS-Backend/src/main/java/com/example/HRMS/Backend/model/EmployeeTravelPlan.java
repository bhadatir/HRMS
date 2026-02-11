package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "employee_travel_plan")
public class EmployeeTravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_employee_travel_plan_id", nullable = false)
    private Long id;

    @ColumnDefault("getdate()")
    @Column(name = "employee_travel_plan_created_at")
    private Instant employeeTravelPlanCreatedAt;

    @NotNull(message = "Travel plan status is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_travel_plan_status_id", nullable = false)
    private TravelPlanStatus fkTravelPlanStatus;

    @NotNull(message = "Employee id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_employee_id", nullable = false)
    private Employee fkEmployee;

    @NotNull(message = "travel plan id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_travel_plan_id", nullable = false)
    private TravelPlan fkTravelPlan;


}