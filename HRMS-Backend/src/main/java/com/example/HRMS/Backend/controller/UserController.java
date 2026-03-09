package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.TravelPlanRepository;
import com.example.HRMS.Backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;

    @GetMapping("/email")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.getEmployeeByEmail(email));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponse>> getEmployee(@RequestParam String query,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(authService.getEmployeeByName(query, page, size));
    }

    @GetMapping("/travel/search")
    public ResponseEntity<Page<EmployeeResponse>> getAvailableEmployeeForTravel(@RequestParam String query,
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
    public ResponseEntity<Page<EmployeeResponse>> getAvailableParticipants(@RequestParam String query,
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

    @PatchMapping("/update-password")
    public ResponseEntity<Object> updatePassword(@RequestParam Long empId, @RequestParam String newPassword) {
        authService.updatePassword(empId, newPassword);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PostMapping("/logout")
    public void logout() {
        authService.logout();
    }

        @GetMapping("/global-search")
    public ResponseEntity<GlobalSearchResponse> globalSearch(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<GlobalSearchResult> employeePage = employeeRepository
                .findAllBySearchTerm(searchTerm, pageable)
                .map(e -> new GlobalSearchResult(
                        e.getId(),
                        e.getEmployeeFirstName() + " " + e.getEmployeeLastName(),
                        e.getFkRole().getRoleName(),
                        "EMPLOYEE" ));

        Page<GlobalSearchResult> travelPage = travelPlanRepository
                .findTravelPlans(searchTerm, pageable)
                .map(t -> new GlobalSearchResult(
                        t.getId(),
                        "Trip to " + t.getTravelPlanTo(),
                        t.getTravelPlanDetails(),
                        "TRAVEL_PLAN" ));

        return ResponseEntity.ok(new GlobalSearchResponse(employeePage, travelPage));
    }

}
