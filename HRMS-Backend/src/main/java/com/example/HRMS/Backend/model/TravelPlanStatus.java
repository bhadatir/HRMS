package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "travel_plan_status")
public class TravelPlanStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_travel_plan_status_id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull(message = "travel plan status id is required")
    @Column(name = "travel_plan_status_name", nullable = false, length = 20)
    private String travelPlanStatusName;


}