package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class EmployeeResponse {

        private Long id;

        private String employeeFirstName;

        private String employeeLastName;

        private String employeeEmail;

        private LocalDate employeeDob;

        private String employeeGender;

        private String employeeProfileUrl;

        private LocalDate employeeHireDate;

        private Integer employeeSalary;

        private Boolean employeeIsActive;

        private Instant employeeCreatedAt;

        private Instant lastLoginAt;

        private Long  departmentId;
        private String  departmentName;

        private Long  positionId;
        private String  positionName;

        private Long  roleId;
        private String  roleName;

        private Long  managerEmployeeId;
        private String  managerEmployeeEmail;


        private String isFirstLogin = "";
}
