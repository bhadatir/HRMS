package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.model.EmployeeTravelPlan;
import com.example.HRMS.Backend.model.Expense;
import com.example.HRMS.Backend.model.ExpenseProof;
import com.example.HRMS.Backend.model.TravelPlanStatus;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;
    private final TravelPlanStatusRepository travelPlanStatusRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseProofTypeRepository expenseProofTypeRepository;
    private final ExpenseProofRepository expenseProofRepository;

    @Override
    public void saveExpense(ExpenseRequest expenseRequest){
        Expense expense = new Expense();
        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(expenseRequest.getFkEmployeeTravelPlanId());
        TravelPlanStatus travelPlanStatus = travelPlanStatusRepository.findTravelPlanStatusById(expenseRequest.getFkExpenseTravelPlanStatusId());
        expense.setExpenseAmount(expenseRequest.getExpenseAmount());
        expense.setExpenseDate(expenseRequest.getExpenseDate());
        expense.setExpenseRemark(expenseRequest.getExpenseRemark());
        expense.setFkEmployeeTravelPlan(employeeTravelPlan);
        expense.setFkExpenseTravelPlanStatus(travelPlanStatus);
        expenseRepository.save(expense);
    }

    @Value("${img.path}")
    private String folderPath;

    @Override
    public void saveExpenseProof(Long proofTypeId, Long expenseId, MultipartFile file) throws IOException {
        ExpenseProof expenseProof = new ExpenseProof();

        String filePath = folderPath + proofTypeId+" " + expenseId + "_" + file.getOriginalFilename();
        file.transferTo(new File(System.getProperty("user.dir") + "/" + filePath));

        expenseProof.setFkExpense(expenseRepository.findExpensesById(expenseId));
        expenseProof.setFkExpenseProofType(expenseProofTypeRepository.findExpenseProofTypeById(proofTypeId));
        expenseProof.setExpenseProofUploadedAt(Instant.now());
        expenseProof.setExpenseProofUrl(filePath);

        expenseProofRepository.save(expenseProof);
    }
}
