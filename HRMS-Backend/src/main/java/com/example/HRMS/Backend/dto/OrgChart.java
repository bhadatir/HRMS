package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrgChart {
    private Long employeeId;
    private String employeeEmail;
    private String firstName;
    private String lastName;
    private String positionName;
    private String departmentName;
    private String employeeProfileUrl;

    private List<OrgChart> managerChain;

    private List<OrgChart> directReports;
}
