package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_department_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull(message = "department name is required")
    @Column(name = "department_name", nullable = false)
    private String departmentName;


}