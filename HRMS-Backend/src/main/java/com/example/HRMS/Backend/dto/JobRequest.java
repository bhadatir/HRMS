package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Data
public class JobRequest {

        @Size(max = 50)
        @NotNull(message = "job title is required")
        private String jobTitle;

        @NotNull(message = "job salary is required")
        private Integer jobSalary;

        @NotNull(message = "job type id is required")
        private Long fkJobTypeId;

        @NotNull(message = "job owner(HR) id is required")
        private Long fkJobOwnerEmployeeId;

        private String reasonForDeActiveJob;

}
