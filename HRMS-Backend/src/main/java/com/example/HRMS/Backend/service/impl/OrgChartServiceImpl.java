package com.example.HRMS.Backend.service.impl;


import com.example.HRMS.Backend.dto.OrgChart;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgChartServiceImpl {

    private final EmployeeRepository empRepo;

    private final ModelMapper modelMapper;

    @Autowired
    public OrgChartServiceImpl(EmployeeRepository employeeRepository,
                               ModelMapper modelMapper)
    {
        this.empRepo=employeeRepository;
        this.modelMapper=modelMapper;
    }

    public OrgChart getEmployeeOrgChart(Long empId) {
        Employee employee = empRepo.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        OrgChart dto = modelMapper.map(employee, OrgChart.class);

        List<Object[]> parentChain = empRepo.getUpwardManagerChainEntities(empId);

        dto.setManagerChain(
                parentChain.stream()
                        .map(e -> {
                            OrgChart oc = new OrgChart();
                            oc.setEmployeeId((Long) e[0]);
                            Employee employee1 = empRepo.findEmployeeById(oc.getEmployeeId());
                            oc.setFirstName((String) e[1]);
                            oc.setLastName((String) e[2]);
                            oc.setDepartmentName(employee1.getFkDepartment().getDepartmentName());
                            oc.setPositionName(employee1.getFkPosition().getPositionName());
                            return oc;
                        })
                        .collect(Collectors.toList())
        );

        List<Employee> reports = empRepo.findByFkManagerEmployeeId(empId);
        dto.setDirectReports(reports.stream()
                .map(e -> modelMapper.map(e, OrgChart.class))
                .collect(Collectors.toList()));

        return dto;
    }
}
