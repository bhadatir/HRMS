package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Department;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Position;
import com.example.HRMS.Backend.model.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

        private Long  departmentId;
        private String  departmentName;

        private Long  positionId;
        private String  positionName;

        private Long  roleId;
        private String  roleName;

        private Long  managerEmployeeId;
        private String  managerEmployeeEmail;


}
