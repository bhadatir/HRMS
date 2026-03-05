package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TravelDocResponse {

    private Long id;

    private String travelDocUrl;

    private Instant travelDocUploadedAt;

    private Long travelDocsTypeId;

    private String travelDocsTypeName;

    private Long travelPlanId;

    private String travelPlanName;

    private Long employeeTravelPlanId;

    private Long employeeId;

    private String employeeEmail;

    private Long employeeFkManagerEmployeeId;

    private String fkRoleRoleName;

    private Boolean docIsDeletedFromTravel;
}
