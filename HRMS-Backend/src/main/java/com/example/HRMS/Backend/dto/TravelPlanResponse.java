package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class TravelPlanResponse {

    private Long id;

    private String travelPlanName;

    private String travelPlanDetails;

    private String travelPlanFrom;

    private String travelPlanTo;

    private Boolean travelPlanIsReturn;

    private LocalDate travelPlanStartDate;

    private LocalDate travelPlanEndDate;

    private Integer travelMaxExpenseAmountPerDay;

    private Instant travelPlanCreatedAt;

    private Long employeeId;

    private String employeeEmail;
    private String employeeFirstName;
    private String employeeLastName;

    private Long employeeFkManagerEmployeeId;

    private Boolean travelPlanIsDeleted;

    private String reasonForDeleteTravelPlan;

    private List<EmployeeTravelPlanResponse> employeeTravelPlanResponses;
}
