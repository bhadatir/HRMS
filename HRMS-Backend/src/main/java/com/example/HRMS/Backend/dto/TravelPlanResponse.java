package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
