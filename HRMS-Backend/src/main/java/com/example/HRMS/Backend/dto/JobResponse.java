package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class JobResponse {

    private Long id;

    private String jobTitle;

    private Integer jobSalary;

    private Instant jobCreatedAt;

    private String jobDescriptionUrl;

    private Boolean jobIsActive;

    private String jobTypeName;

    private String employeeEmail;

}
