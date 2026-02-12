package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;
    private final TravelPlanStatusRepository travelPlanStatusRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseProofTypeRepository expenseProofTypeRepository;
    private final ExpenseProofRepository expenseProofRepository;
    private final EmailService emailService;

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

        List<String> emails = new ArrayList<>();
        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();
        Employee employee = travelPlan.getFkTravelPlanHREmployee();
        emails.add(employee.getEmployeeEmail());

        emailService.sendEmail(emails,
                "Review Expense",
                "Employee detail : " + employee.getId() +" "+ employee.getEmployeeEmail()
                + "travel plan detail : " + travelPlan.getTravelPlanDetails() + " "
                + travelPlan.getTravelPlanTo() + " - " + travelPlan.getTravelPlanFrom());
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
