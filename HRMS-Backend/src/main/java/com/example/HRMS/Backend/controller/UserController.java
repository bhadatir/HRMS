package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.EmployeeResponse;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.AuthService;
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
public class UserController {

    private final AuthService authService;

    @GetMapping("/email")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(@RequestParam String email) throws IOException {
        return ResponseEntity.ok(authService.getEmployeeByEmail(email));
    }

    @PatchMapping("/profileImage/{empId}")
    public ResponseEntity<String> addProfileImage(@PathVariable Long empId,
                                                  @RequestBody MultipartFile file) throws IOException {
        authService.addProfileImage(empId,file);
        return ResponseEntity.ok("Profile Image set successfully");
    }
}
