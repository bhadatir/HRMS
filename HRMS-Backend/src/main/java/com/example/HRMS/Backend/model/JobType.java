package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "job_type")
public class JobType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_job_type_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull(message = "job type name is required")
    @Column(name = "job_type_name", nullable = false)
    private String jobTypeName;


}