package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.ExpenseProofResponse;
import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.dto.ExpenseResponse;
import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;
    private final TravelPlanStatusRepository travelPlanStatusRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseProofTypeRepository expenseProofTypeRepository;
    private final ExpenseProofRepository expenseProofRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final TravelDocRepository travelDocRepository;

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

    @Override
    public List<ExpenseResponse> getExpenseById(Long employeeId, Long travelPlanId){
        Long employeeTravelPlanId = employeeTravelPlanRepository.findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(employeeId,travelPlanId);

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);
        List<Expense> expenses = expenseRepository.findExpenseByFkEmployeeTravelPlan(employeeTravelPlan);

        List<ExpenseResponse> expenseResponses= new ArrayList<>();
        for(Expense expense: expenses){
            ExpenseResponse expenseResponse = new ExpenseResponse();

            expenseResponse.setExpenseDate(expense.getExpenseDate());
            expenseResponse.setExpenseAmount(expense.getExpenseAmount());
            expenseResponse.setExpenseRemark(expense.getExpenseRemark());
            expenseResponse.setExpenseUploadedAt(expense.getExpenseUploadedAt());

            //it is correct??
            expenseResponse.setId(expense.getId());

            expenseResponse.setEmployeeTravelPlanId(expense.getFkEmployeeTravelPlan().getId());

            expenseResponse.setExpenseTravelPlanStatusId(expense.getFkExpenseTravelPlanStatus().getId());
            expenseResponse.setExpenseTravelPlanStatusName(expense.getFkExpenseTravelPlanStatus().getTravelPlanStatusName());

            List<ExpenseProof> expenseProofs = expenseProofRepository.findExpenseProofByFkExpense_Id(expense.getId());
            List<ExpenseProofResponse> expenseProofResponses = new ArrayList<>();
            for(ExpenseProof expenseProof : expenseProofs){
                ExpenseProofResponse expenseProofResponse = modelMapper.map(expenseProof,ExpenseProofResponse.class);
                expenseProofResponses.add(expenseProofResponse);
            }

            expenseResponse.setExpenseProofResponses(expenseProofResponses);

            expenseResponses.add(expenseResponse);
        }

        return expenseResponses;
    }

    @Override
    public List<ExpenseProofResponse> getExpenseProofById(Long expenseId){
        List<ExpenseProof> expenseProofs = expenseProofRepository.findExpenseProofByFkExpense_Id(expenseId);
        List<ExpenseProofResponse> expenseProofResponses = new ArrayList<>();
        for(ExpenseProof expenseProof : expenseProofs){
            ExpenseProofResponse expenseProofResponse = modelMapper.map(expenseProof,ExpenseProofResponse.class);
            expenseProofResponses.add(expenseProofResponse);
        }
        return expenseProofResponses;
    }

    @Value("${img.path}")
    private String folderPath;

    @Value("${URL.path}")
    private String URL;

    @Override
    public void saveExpenseProof(Long proofTypeId, Long expenseId, MultipartFile file) throws IOException {
        ExpenseProof expenseProof = new ExpenseProof();

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "expanse_proof/" + proofTypeId +"_" + expenseId + "_" + originalFilePath;
        file.transferTo(new File(System.getProperty("user.dir") + "/" + folderPath + filePath));

        expenseProof.setFkExpense(expenseRepository.findExpensesById(expenseId));
        expenseProof.setFkExpenseProofType(expenseProofTypeRepository.findExpenseProofTypeById(proofTypeId));
        expenseProof.setExpenseProofUploadedAt(Instant.now());
        expenseProof.setExpenseProofUrl(filePath);

        expenseProofRepository.save(expenseProof);
    }

    @Override
    public void updateExpenseStatus(Long expId, Long statusId){
        Expense expense = expenseRepository.findExpensesById(expId);
        expense.setFkExpenseTravelPlanStatus(travelPlanStatusRepository.findTravelPlanStatusById(statusId));
        expenseRepository.save(expense);
    }

    @Override
    public void saveExpenseWithProof(@Valid ExpenseRequest expenseRequest, List<MultipartFile> files, List<Long> proofTypeId) throws IOException {
        Instant time = Instant.now();
        Expense expense = new Expense();
        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(expenseRequest.getFkEmployeeTravelPlanId());
        TravelPlanStatus travelPlanStatus = travelPlanStatusRepository.findTravelPlanStatusById(expenseRequest.getFkExpenseTravelPlanStatusId());
        expense.setExpenseAmount(expenseRequest.getExpenseAmount());
        expense.setExpenseDate(expenseRequest.getExpenseDate());
        expense.setExpenseRemark(expenseRequest.getExpenseRemark());
        expense.setFkEmployeeTravelPlan(employeeTravelPlan);
        expense.setExpenseUploadedAt(time);
        expense.setFkExpenseTravelPlanStatus(travelPlanStatus);
        expenseRepository.save(expense);

        String timeInString = Instant.now().toString().replace(":","-");

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Long id = proofTypeId.get(i);

            ExpenseProof expenseProof = new ExpenseProof();

            String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
            String filePath = "expanse_proof/" + id +"_" + timeInString + "_" + originalFilePath;
            file.transferTo(new File(System.getProperty("user.dir") + "/" + folderPath + filePath));

            expenseProof.setFkExpense(expense);
            expenseProof.setFkExpenseProofType(expenseProofTypeRepository.findExpenseProofTypeById(id));
            expenseProof.setExpenseProofUploadedAt(Instant.now());
            expenseProof.setExpenseProofUrl(URL + filePath);

            expenseProofRepository.save(expenseProof);

        }
    }

}
