package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.EmployeeResponse;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.PostService;
import com.example.HRMS.Backend.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173",allowCredentials = "true")
public class UserController {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/email")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(@RequestParam String email) {
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow( () -> new RuntimeException("employee not found"));
        EmployeeResponse employeeResponse = modelMapper.map(employee,EmployeeResponse.class);
        return ResponseEntity.ok(employeeResponse);
    }
}
