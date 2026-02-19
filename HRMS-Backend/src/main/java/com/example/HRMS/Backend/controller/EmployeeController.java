package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.PostService;
import com.example.HRMS.Backend.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final TravelPlanService travelPlanService;
    private final TravelDocRepository travelDocRepository;
    private final EmployeeRepository employeeRepository;
    private final PostService postService;

    @PostMapping("/travelPlanDoc/{employeeId}/{employeeTravelPlanId}/{docType}")
    public ResponseEntity<String> addTravelPlanDocByEmployee(@PathVariable Long employeeTravelPlanId, @PathVariable Long employeeId
                                                            , @RequestParam("file") MultipartFile file, @PathVariable("docType") Long docTypeId ) throws IOException {
        travelPlanService.saveDocByEmployee(employeeTravelPlanId, file, docTypeId, employeeId);
        return ResponseEntity.ok("Travel plan doc add successfully");
    }

    @GetMapping("/travelPlanDoc")
    public ResponseEntity<List<TravelDoc>> showTravelDocByEmpIdByEmployee() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow();

        Long id = employee.getId();
        List<TravelDoc> travelDocs = travelDocRepository.findTravelDocByFkEmployee_Id(id);

        return ResponseEntity.ok(travelDocs);
    }


}
