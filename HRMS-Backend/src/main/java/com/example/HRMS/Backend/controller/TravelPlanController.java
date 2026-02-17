package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.service.ExpenseService;
import com.example.HRMS.Backend.service.TravelPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;
    private final ExpenseService expenseService;

    @GetMapping("/allTravelPlans")
    public ResponseEntity<List<TravelPlanResponse>> showAllTravelPlans() {
        return ResponseEntity.ok(travelPlanService.showAllTravelPlan());
    }

    @GetMapping("/travelPlanId/{id}")
    public ResponseEntity<TravelPlanResponse> showTravelPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(travelPlanService.showTravelPlanById(id));
    }

    @GetMapping("/hrId/{hrEmpId}")
    public ResponseEntity<TravelPlan> findTravelPlanByHrEmployeeId(@PathVariable Long hrEmpId){
        return ResponseEntity.ok(travelPlanService.findTravelPlanByHREmployeeId(hrEmpId));
    }

    @PostMapping("/expense")
    public ResponseEntity<String> addExpense(@Valid @RequestBody ExpenseRequest expenseRequest) {
        expenseService.saveExpense(expenseRequest);
        return ResponseEntity.ok("Expense add successfully");
    }

    @GetMapping("/expense/{employeeId}/{travelPlanId}")
    public ResponseEntity<List<ExpenseResponse>> getExpenseById(@PathVariable Long travelPlanId
                                                                , @PathVariable Long employeeId) {
        return ResponseEntity.ok(expenseService.getExpenseById(employeeId, travelPlanId));
    }

    @GetMapping("/expenseProof/{expenseId}")
    public ResponseEntity<List<ExpenseProofResponse>> getExpenseProofById(@PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseProofById(expenseId));
    }

    @PostMapping(value = "/expenseWithProof", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addExpenseWithProof(@Valid @RequestPart ExpenseRequest expenseRequest
                                                        , @RequestPart("files") List<MultipartFile> files
                                                        , @RequestPart("proofTypes") List<Long> proofTypeId) throws IOException {
        expenseService.saveExpenseWithProof(expenseRequest, files, proofTypeId);
        return ResponseEntity.ok("Expense add successfully");
    }

    @GetMapping("/employeeTravelPlan/{empId}/{travelId}")
    public ResponseEntity<Long> findEmployeeTravelPlanId(@PathVariable Long empId,
                                                         @PathVariable Long travelId) {
        return ResponseEntity.ok(travelPlanService.findEmployeeTravelPlanId(empId,travelId));
    }

    @PostMapping("/expenseProof/{proofTypeId}/{expenseId}")
    public ResponseEntity<String> addExpenseProof(@PathVariable Long proofTypeId,
                                                  @PathVariable Long expenseId,
                                                  @RequestParam("file") MultipartFile file) throws IOException {
        expenseService.saveExpenseProof(proofTypeId, expenseId, file);
        return ResponseEntity.ok("Expense Proof add successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Long>> getTravelplan(@RequestParam String query) throws IOException {
        return ResponseEntity.ok(travelPlanService.getTravelPlan(query));
    }

    @GetMapping("/travelDoc/{empId}/{travelPlanId}")
    public ResponseEntity<List<TravelDocResponse>> showTravelDocByEmpId(@PathVariable Long empId,
                                                                        @PathVariable Long travelPlanId) {
        return ResponseEntity.ok(travelPlanService.findTravelDocByFkEmployeeId(empId,travelPlanId));
    }

    @GetMapping("/travelDoc/{travelPlanId}")
    public ResponseEntity<List<TravelDocResponse>> showAllTravelPlanDocByTravelPlan(@PathVariable Long travelPlanId) throws IOException {
        return ResponseEntity.ok(travelPlanService.findAllTravelPlanDocByTravelPlan(travelPlanId));
    }

}
