package com.example.HRMS.Backend.controller;


import com.example.HRMS.Backend.dto.EmployeeResponse;
import com.example.HRMS.Backend.dto.RegisterRequest;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.dto.TravelPlanResponse;
import com.example.HRMS.Backend.model.Department;
import com.example.HRMS.Backend.model.Position;
import com.example.HRMS.Backend.model.Role;
import com.example.HRMS.Backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @PutMapping("/user/{userEmail}")
    public ResponseEntity<String> updateUser(@Valid @RequestBody RegisterRequest registerRequest, @PathVariable String userEmail){
        authService.updateUser(registerRequest,userEmail);
        return ResponseEntity.ok("user Update successfully");
    }

    @PatchMapping("/inActiveUser/{userId}")
    public ResponseEntity<String> inActiveUserByID(@PathVariable Long userId, @RequestParam String reason){
        authService.inActiveUserById(userId, reason);
        return ResponseEntity.ok("user inActive successfully");
    }

    @GetMapping("/allPositions")
    public ResponseEntity<List<Position>> showAllPositions() {
        return ResponseEntity.ok(authService.showAllPosition());
    }

    @GetMapping("/allDepartments")
    public ResponseEntity<List<Department>> showAllDepartments() {
        return ResponseEntity.ok(authService.showAllDepartments());
    }

    @GetMapping("/")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(@RequestParam String searchTerm) {
        return ResponseEntity.ok(authService.getAllEmployees(searchTerm));
    }

    @GetMapping("/allRoles")
    public ResponseEntity<List<Role>> showAllRoles() {
        return ResponseEntity.ok(authService.showAllRoles());
    }
}
