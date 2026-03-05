package com.example.HRMS.Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TravelPlanRequest {

    @NotNull(message = "travel plan name is required")
    private String travelPlanName;

    @NotNull(message = "travel plan detail is required")
    private String travelPlanDetails;

    @NotNull(message = "travel plan starting location is required")
    private String travelPlanFrom;

    @NotNull(message = "travel plan ending location is required")
    private String travelPlanTo;

    private Boolean travelPlanIsReturn;

    private String reasonForDeleteTravelPlan;

    @NotNull(message = "travel plan starting date is required")
    private LocalDate travelPlanStartDate;

    @NotNull(message = "travel plan ending date is required")
    private LocalDate travelPlanEndDate;

    @NotNull(message = "Travel Plan Max Expense Amount Per Day is required")
    private Integer travelMaxExpenseAmountPerDay;

    @NotNull(message = "HR Employee id is required who create this travel plan")
    private Long fkTravelPlanHREmployeeId;

    @NotNull(message = "add minimum one employee")
    private List<Long> employeesInTravelPlanId;

}
