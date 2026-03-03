package com.example.HRMS.Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull(message = "employee first name is required")
    private String firstName;

    @NotNull(message = "employee last name is required")
    private String lastName;

    @NotNull(message = "employee email is required")
    @Email(message = "employee email must be in proper formate")
    private String email;

    @NotNull(message = "employee password is required")
    private String password;

    @NotNull(message = "employee DOB is required")
    @Past(message = "employee DOB must be past")
    private LocalDate dob;

    @NotNull(message = "employee gender is required")
    private String gender;

    @NotNull(message = "employee hire date is required")
    private LocalDate hireDate;

    @NotNull(message = "employee salary is required")
    @Min(value = 0, message = "employee salary can not be -ve")
    private Integer salary;

    @NotNull(message = "employee department is required")
    private Integer departmentId;

    @NotNull(message = "employee position is required")
    private Integer positionId;

    @NotNull(message = "employee role is required")
    private Integer roleId;
}
