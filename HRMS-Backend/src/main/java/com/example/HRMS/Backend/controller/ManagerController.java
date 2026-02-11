package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final TravelPlanService travelPlanService;
    private final TravelDocRepository travelDocRepository;
    private final EmployeeRepository employeeRepository;

//    @GetMapping("/{id}")
//    public ResponseEntity<List<TravelDoc>> showTravelDocByEmpIdByManager(@PathVariable Long id) {
//
//    }

}
