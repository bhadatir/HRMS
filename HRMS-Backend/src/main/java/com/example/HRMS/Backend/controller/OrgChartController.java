package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.OrgChart;
import com.example.HRMS.Backend.service.impl.OrgChartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/org-chart")
public class OrgChartController {

    private final OrgChartServiceImpl orgService;
    @Autowired
    public OrgChartController(OrgChartServiceImpl orgChartService)
    {
        this.orgService=orgChartService;
    }

    @GetMapping("/{empId}")
    public ResponseEntity<OrgChart> getHierarchy(@PathVariable Long empId) {
        return ResponseEntity.ok(orgService.getEmployeeOrgChart(empId));
    }
}