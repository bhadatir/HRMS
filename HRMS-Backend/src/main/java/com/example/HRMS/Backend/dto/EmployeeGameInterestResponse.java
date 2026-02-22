package com.example.HRMS.Backend.dto;

import lombok.Data;

@Data
public class EmployeeGameInterestResponse {
    private Long id;

    private Long employeeId;

    private String employeeEmail;

    private Long gameTypeId;

    private String gameTypeName;
}
