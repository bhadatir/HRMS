package com.example.HRMS.Backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobRequest {

        @Size(max = 50)
        @NotBlank(message = "job title is required")
        private String jobTitle;

        @NotNull(message = "job salary is required")
        @Min(value = 1,message = "Salary cannot be negative")
        private Integer jobSalary;

        @NotNull(message = "job type id is required")
        private Long fkJobTypeId;

        @NotNull(message = "job owner(HR) id is required")
        private Long fkJobOwnerEmployeeId;

        private String reasonForDeActiveJob;

}
