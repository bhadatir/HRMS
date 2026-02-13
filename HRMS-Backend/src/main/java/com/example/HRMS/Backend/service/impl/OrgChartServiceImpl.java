package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.OrgChart;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.service.OrgChartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgChartServiceImpl implements OrgChartService {

    private final EmployeeRepository empRepo;

    private final ModelMapper modelMapper;

    //it set parent chain and report of sub employees for given employee id
    @Override
    public OrgChart getEmployeeOrgChart(Long empId) {
        Employee employee = empRepo.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        OrgChart dto = modelMapper.map(employee, OrgChart.class);

        List<Object[]> parentChain = empRepo.getUpwardManagerChainEntities(empId);

        dto.setManagerChain(
                parentChain.stream()
                        .map(object -> {
                            OrgChart oc = new OrgChart();
                            oc.setEmployeeId((Long) object[0]);
                            Employee employee1 = empRepo.findEmployeeById(oc.getEmployeeId());
                            oc.setFirstName((String) object[1]);
                            oc.setLastName((String) object[2]);
                            oc.setDepartmentName(employee1.getFkDepartment().getDepartmentName());
                            oc.setPositionName(employee1.getFkPosition().getPositionName());
                            oc.setEmployeeProfileUrl(employee1.getEmployeeProfileUrl());
                            return oc;
                        }).toList()
        );

        List<Employee> reports = empRepo.findByFkManagerEmployeeId(empId);
        dto.setDirectReports(reports.stream()
                .map(emp -> modelMapper.map(emp, OrgChart.class))
                .toList()
        );

        return dto;
    }
}
