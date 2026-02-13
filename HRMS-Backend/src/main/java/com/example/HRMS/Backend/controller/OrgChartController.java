package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.OrgChart;
import com.example.HRMS.Backend.service.OrgChartService;
import com.example.HRMS.Backend.service.impl.OrgChartServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/org-chart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173",allowCredentials = "true")
public class OrgChartController {

    private final OrgChartService orgService;

    @GetMapping("/{empId}")
    public ResponseEntity<OrgChart> getHierarchy(@PathVariable Long empId) {
        return ResponseEntity.ok(orgService.getEmployeeOrgChart(empId));
    }
}