package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.AuthRequest;
import com.example.HRMS.Backend.dto.AuthResponse;
import com.example.HRMS.Backend.dto.RegisterRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.DepartmentRepository;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.PositionRepository;
import com.example.HRMS.Backend.repository.RoleRepository;
import com.example.HRMS.Backend.service.AuthService;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final EmployeeRepository employeeRepository;

    private final RoleRepository roleRepository;

    private final DepartmentRepository departmentRepository;

    private final PositionRepository positionRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, EmployeeRepository employeeRepository, RoleRepository roleRepository, DepartmentRepository departmentRepository, PositionRepository positionRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.positionRepository = positionRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        return new AuthResponse(jwt);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        if (employeeRepository.existsByEmployeeEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Employee employee = new Employee();
        employee.setEmployeeFirstName(request.getFirstName());
        employee.setEmployeeLastName(request.getLastName());
        employee.setEmployeeEmail(request.getEmail());
        employee.setEmployeePassword(passwordEncoder.encode(request.getPassword()));
        employee.setEmployeeDob(request.getDob());
        employee.setEmployeeGender(request.getGender());
        employee.setEmployeeHireDate(request.getHireDate());
        employee.setEmployeeSalary(request.getSalary());

        Department department = departmentRepository.findById(Long.valueOf(request.getDepartmentId()))
                .orElseThrow(() -> new RuntimeException("Department not found"));
        employee.setFkDepartment(department);

        Position position = positionRepository.findById(Long.valueOf(request.getPositionId()))
                .orElseThrow(() -> new RuntimeException("Position not found"));
        employee.setFkPosition(position);

        Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
                .orElseThrow(() -> new RuntimeException("Role not found"));
        employee.setFkRole(role);

        employeeRepository.save(employee);
    }

    @Override
    public void initiateForgotPassword(String email) {
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        employee.setResetToken(token);
        employee.setResetTokenExpiry(LocalDateTime.now().plusMinutes(1));
        employeeRepository.save(employee);

        List<String> emails = new ArrayList<>();
        emails.add(email);

        emailService.sendEmail(emails, "Password Reset", "Your token is: " + token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Employee employee = employeeRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (employee.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        employee.setEmployeePassword(passwordEncoder.encode(newPassword));
        employee.setResetToken(null);
        employeeRepository.save(employee);
    }
}
