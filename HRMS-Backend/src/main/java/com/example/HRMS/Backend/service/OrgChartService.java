package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.OrgChart;

public interface OrgChartService {
    OrgChart getEmployeeOrgChart(Long empId);
}
