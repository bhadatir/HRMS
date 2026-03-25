package com.example.HRMS.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class JobShareRequest {

    @NotNull(message = "share by whom is required")
    private Long fkJobShareEmployeeId;

    @NotNull(message = "which job you share? that job id is required")
    private Long fkJobId;

    @NotBlank(message = "receivers email id is required")
    private List<String> emails;
}
