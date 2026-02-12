package com.example.HRMS.Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotNull
    @Past
    private LocalDate dob;
    @NotBlank
    private String gender;
    @NotNull
    @PastOrPresent
    private LocalDate hireDate;
    @NotNull
    @Min(0)
    private Integer salary;
    @NotNull
    private Integer departmentId;
    @NotNull
    private Integer positionId;
    @NotNull
    private Integer roleId;
}
