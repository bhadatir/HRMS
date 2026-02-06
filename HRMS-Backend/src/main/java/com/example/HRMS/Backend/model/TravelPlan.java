package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
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
@Table(name = "travel_plan")
public class TravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_travel_plan_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull(message = "travel plan name is required")
    @Column(name = "travel_plan_name", nullable = false)
    private String travelPlanName;

    @Size(max = 255)
    @NotNull(message = "travel plan detail is required")
    @Column(name = "travel_plan_details", nullable = false)
    private String travelPlanDetails;

    @Size(max = 255)
    @NotNull(message = "travel plan starting location is required")
    @Column(name = "travel_plan_from", nullable = false)
    private String travelPlanFrom;

    @Size(max = 255)
    @NotNull(message = "travel plan ending location is required")
    @Column(name = "travel_plan_to", nullable = false)
    private String travelPlanTo;

    @ColumnDefault("1")
    @Column(name = "travel_plan_is_return")
    private Boolean travelPlanIsReturn;

    @NotNull(message = "travel plan starting date is required")
    @Column(name = "travel_plan_start_date", nullable = false)
    private LocalDate travelPlanStartDate;

    @NotNull(message = "travel plan ending date is required")
    @Column(name = "travel_plan_end_date", nullable = false)
    private LocalDate travelPlanEndDate;

    @ColumnDefault("getdate()")
    @Column(name = "travel_plan_created_at")
    private Instant travelPlanCreatedAt;

    @NotNull(message = "Employee id is required who create this travel plan")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_travel_plan_employee_id", nullable = false)
    private Employee fkTravelPlanEmployee;


}