package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.ExpenseProofResponse;
import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.dto.ExpenseResponse;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.ExpenseService;
import com.example.HRMS.Backend.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;
    private final ExpenseStatusRepository expenseStatusRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseProofTypeRepository expenseProofTypeRepository;
    private final ExpenseProofRepository expenseProofRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final TravelDocRepository travelDocRepository;
    private final NotificationService notificationService;

    @Override
    public void saveExpense(ExpenseRequest expenseRequest){
        Expense expense = new Expense();
        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(expenseRequest.getFkEmployeeTravelPlanId());

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be add expenses.");
        }

        LocalDate travelEndDate = travelPlan.getTravelPlanStartDate();
        LocalDate travelAddDeadLine = travelEndDate.plusDays(10);

        if(!LocalDate.now().isBefore(travelAddDeadLine) && !LocalDate.now().isAfter(travelEndDate)){
            throw new RuntimeException("only add expense between travel plan end date and after ending travel plan 10 days duration.");
        }

        ExpenseStatus expenseStatus = expenseStatusRepository.findExpenseStatusById(1L);
        expense.setExpenseAmount(expenseRequest.getExpenseAmount());
        expense.setExpenseDate(expenseRequest.getExpenseDate());
        expense.setExpenseRemark(expenseRequest.getExpenseRemark());
        expense.setFkEmployeeTravelPlan(employeeTravelPlan);
        expense.setFkExpenseExpenseStatus(expenseStatus);
        expenseRepository.save(expense);

        List<String> emails = new ArrayList<>();
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

            expenseResponse.setExpenseExpenseStatusId(expense.getFkExpenseExpenseStatus().getId());
            expenseResponse.setExpenseExpenseStatusName(expense.getFkExpenseExpenseStatus().getExpenseStatusName());

            if(expense.getFkExpenseExpenseStatus().getId() == 3){
                expenseResponse.setReasonForRejectExpense(expense.getReasonForRejectExpense());
            }
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

        Expense expense = expenseRepository.findExpensesById(expenseId);

        EmployeeTravelPlan employeeTravelPlan =  expense.getFkEmployeeTravelPlan();

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be add expense proof.");
        }

        LocalDate travelEndDate = travelPlan.getTravelPlanStartDate();
        LocalDate travelAddDeadLine = travelEndDate.plusDays(10);

        if(!LocalDate.now().isBefore(travelAddDeadLine) && !LocalDate.now().isAfter(travelEndDate)){
            throw new RuntimeException("only add expense proof between travel plan end date and after ending travel plan 10 days duration.");
        }

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
    public void updateExpenseStatus(Long expId, Long statusId, String reason){
        Expense expense = expenseRepository.findExpensesById(expId);

        EmployeeTravelPlan employeeTravelPlan =  expense.getFkEmployeeTravelPlan();

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be update expense status.");
        }

        if(statusId == 3)expense.setReasonForRejectExpense(reason);
        expense.setFkExpenseExpenseStatus(expenseStatusRepository.findExpenseStatusById(statusId));
        expenseRepository.save(expense);

        notificationService.createNotification(expense.getFkEmployeeTravelPlan().getFkEmployee().getId()
                ,"Expense Status Upgraded"
                , "Status : " + expenseStatusRepository.findExpenseStatusById(statusId).getExpenseStatusName()
                   + " at :" + LocalDateTime.now() + " by "
                   + expense.getFkEmployeeTravelPlan().getFkTravelPlan().getFkTravelPlanHREmployee().getEmployeeEmail() + " for "
                   + expense.getFkEmployeeTravelPlan().getFkTravelPlan().getTravelPlanName() + " travel plan"
        );
    }

    @Override
    public void saveExpenseWithProof(@Valid ExpenseRequest expenseRequest, List<MultipartFile> files, List<Long> proofTypeId) throws IOException {

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(expenseRequest.getFkEmployeeTravelPlanId());

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be be add expenses.");
        }

        LocalDate travelEndDate = travelPlan.getTravelPlanStartDate();
        LocalDate travelAddDeadLine = travelEndDate.plusDays(10);

        if(!LocalDate.now().isBefore(travelAddDeadLine) && !LocalDate.now().isAfter(travelEndDate)){
            throw new RuntimeException("only add expense with proof between travel plan end date and after ending travel plan 10 days duration.");
        }

        Instant time = Instant.now();
        Expense expense = new Expense();
        ExpenseStatus expenseStatus = expenseStatusRepository.findExpenseStatusById(expenseRequest.getFkExpenseExpenseStatusId());
        expense.setExpenseAmount(expenseRequest.getExpenseAmount());
        expense.setExpenseDate(expenseRequest.getExpenseDate());
        expense.setExpenseRemark(expenseRequest.getExpenseRemark());
        expense.setFkEmployeeTravelPlan(employeeTravelPlan);
        expense.setExpenseUploadedAt(time);
        expense.setFkExpenseExpenseStatus(expenseStatus);
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
        notificationService.createNotification(employeeTravelPlan.getFkTravelPlan().getFkTravelPlanHREmployee().getId()
                ,"Expense Added"
                , "at :" + LocalDateTime.now() + " by "
                + employeeTravelPlan.getFkEmployee().getEmployeeEmail() + " for "
                + employeeTravelPlan.getFkTravelPlan().getTravelPlanName() + " travel plan"
        );

    }

}
