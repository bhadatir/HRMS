package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Data
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_employee_id", nullable = false)
    private Long id;

    @Size(max = 25)
    @NotNull(message = "Employee first name is required")
    @Column(name = "employee_first_name", nullable = false)
    private String employeeFirstName;

    @Size(max = 25)
    @NotNull(message = "Employee last name is required")
    @Column(name = "employee_last_name", nullable = false)
    private String employeeLastName;

    @Size(max = 25)
    @NotNull(message = "Employee email address is required")
    @Email(message = "Email is not in perfect formate")
    @Column(name = "employee_email", nullable = false)
    private String employeeEmail;

    @Size(max = 100)
    @NotNull(message = "Employee password is required")
    @Column(name = "employee_password", nullable = false)
    private String employeePassword;

    @PastOrPresent(message = "DOB cannot be in the future")
    @NotNull(message = "Employee DOB is required")
    @Column(name = "employee_dob", nullable = false)
    private LocalDate employeeDob;

    @NotNull(message = "Employee Gender is required")
    @Column(name = "employee_gender", nullable = false)
    private String employeeGender;

    @Column(name = "employee_profile")
    private String employeeProfileUrl;

    @NotNull(message = "Employee hire date is required")
    @Column(name = "employee_hire_date", nullable = false)
    private LocalDate employeeHireDate;

    @NotNull(message = "Employee salary is required")
    @Min(value = 0,message = "Salary cannot be negative")
    @Column(name = "employee_salary", nullable = false)
    private Integer employeeSalary;

    @Column(name = "employee_is_active")
    private Boolean employeeIsActive = true;

    @Column(name = "employee_pass_resetToken")
    private String resetToken;

    @Column(name = "employee_resetToken_expiry")
    private LocalDateTime resetTokenExpiry;

    @NotNull(message = "Employee department id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_department_id", nullable = false)
    private Department fkDepartment;

    @NotNull(message = "Employee position id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_position_id", nullable = false)
    private Position fkPosition;

    @NotNull(message = "Employee role id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_role_id", nullable = false)
    private Role fkRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_manager_employee_id")
    private Employee fkManagerEmployee;

}
