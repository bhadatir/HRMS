package com.example.HRMS.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class EmployeeSearch {
    private Long id;

    private String employeeFirstName;

    private String employeeLastName;

    private String employeeEmail;

    private Long roleId;

    private String roleName;

    private Long positionId;

    private String positionName;

    private Long departmentId;

    private String departmentName;
}


