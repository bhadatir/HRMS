package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.TravelDocsType;
import com.example.HRMS.Backend.model.TravelPlan;
import lombok.Data;

import java.time.Instant;

@Data
public class TravelDocResponse {

    private String travelDocUrl;

    private byte[] travelDocImg;

    private Instant travelDocUploadedAt;

    private Long travelDocsTypeId;

    private String travelDocsTypeName;

    private Long travelPlanId;

    private String travelPlanName;

    private Long employeeTravelPlanId;

    private Long employeeId;

    private String employeeEmail;
}
