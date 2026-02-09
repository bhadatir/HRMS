package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cv_status_type")
public class CvStatusType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_cv_status_type_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull(message = "status type is required")
    @Column(name = "cv_status_type_name", nullable = false)
    private String cvStatusTypeName;


}