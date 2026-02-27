package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.EmployeeResponse;
import com.example.HRMS.Backend.dto.EmployeeSearch;
import com.example.HRMS.Backend.dto.ParticipantsSearch;
import com.example.HRMS.Backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/email")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(@RequestParam String email) throws IOException {
        return ResponseEntity.ok(authService.getEmployeeByEmail(email));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeSearch>> getEmployee(@RequestParam String query,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(authService.getEmployeeByName(query, page, size));
    }

    @GetMapping("/travel/search")
    public ResponseEntity<Page<EmployeeSearch>> getAvailableEmployeeForTravel(@RequestParam String query,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam String startDate,
                                                                     @RequestParam String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate1 = LocalDate.parse(startDate, formatter);
        LocalDate endDate1 = LocalDate.parse(endDate, formatter);
        return ResponseEntity.ok(authService.getAvailableEmployeeForTravel(query, page, size, startDate1, endDate1));
    }

    @GetMapping("/participants/search")
    public ResponseEntity<Page<EmployeeSearch>> getAvailableParticipants(@RequestParam String query,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size,
                                                                             @RequestParam String startDate,
                                                                             @RequestParam Long gameTypeId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
        LocalDateTime startDate1 = LocalDateTime.parse(startDate, formatter);
        return ResponseEntity.ok(authService.getAvailableParticipants(query, page, size, startDate1,gameTypeId));
    }

    @PatchMapping("/profileImage/{empId}")
    public ResponseEntity<String> addProfileImage(@PathVariable Long empId,
                                                  @RequestBody MultipartFile file) throws IOException {
        authService.addProfileImage(empId,file);
        return ResponseEntity.ok("Profile Image set successfully");
    }
}
