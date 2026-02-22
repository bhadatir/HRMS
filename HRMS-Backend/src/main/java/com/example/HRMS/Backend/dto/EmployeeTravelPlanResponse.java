package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class EmployeeTravelPlanResponse {
    private Long id;

    private Instant employeeTravelPlanCreatedAt;

    private Long employeeId;

    private Long employeeFkManagerEmployeeId;

    private String employeeEmail;
    private String employeeFirstName;
    private String employeeLastName;

    private Long travelPlanId;

    private String travelPlanName;

    private Boolean employeeIsDeletedFromTravel;

}
