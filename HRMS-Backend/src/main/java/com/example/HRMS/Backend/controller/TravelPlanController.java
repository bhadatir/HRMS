package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.GameTypeRepository;
import com.example.HRMS.Backend.repository.TravelDocRepository;
import com.example.HRMS.Backend.service.ExpenseService;
import com.example.HRMS.Backend.service.TravelPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;
    private final ExpenseService expenseService;

    @GetMapping("/allTravelPlans")
    public ResponseEntity<List<TravelPlan>> showAllGames() {
        return ResponseEntity.ok(travelPlanService.showAllTravelPlan());
    }

    @GetMapping("/{hrEmpId}")
    public ResponseEntity<TravelPlan> findTravelPlanByHrEmployeeId(@PathVariable Long hrEmpId){
        return ResponseEntity.ok(travelPlanService.findTravelPlanByHREmployeeId(hrEmpId));
    }

    @PostMapping("/expense")
    public ResponseEntity<String> addExpense(@Valid @RequestBody ExpenseRequest expenseRequest) {
        expenseService.saveExpense(expenseRequest);
        return ResponseEntity.ok("Expense add successfully");
    }

    @PostMapping("/expenseProof/{proofTypeId}/{expenseId}")
    public ResponseEntity<String> addExpenseProof(@PathVariable Long proofTypeId,
                                                  @PathVariable Long expenseId,
                                                  @RequestParam("file") MultipartFile file) throws IOException {
        expenseService.saveExpenseProof(proofTypeId, expenseId, file);
        return ResponseEntity.ok("Expense Proof add successfully");
    }
}
