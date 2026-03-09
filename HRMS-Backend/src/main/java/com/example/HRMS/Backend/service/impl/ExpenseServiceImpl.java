package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.ExpenseProofResponse;
import com.example.HRMS.Backend.dto.ExpenseRequest;
import com.example.HRMS.Backend.dto.ExpenseResponse;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.AuthService;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.ExpenseService;
import com.example.HRMS.Backend.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    private final EmployeeRepository employeeRepository;
    private final ExpenseStatusRepository expenseStatusRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseProofTypeRepository expenseProofTypeRepository;
    private final ExpenseProofRepository expenseProofRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final AuthService authService;

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
            expenseResponse.setEmployeeEmail(employeeRepository.findEmployeeById(employeeId).getEmployeeEmail());

            expenseResponse.setId(expense.getId());

            expenseResponse.setEmployeeTravelPlanId(expense.getFkEmployeeTravelPlan().getId());

            expenseResponse.setExpenseExpenseStatusId(expense.getFkExpenseExpenseStatus().getId());
            expenseResponse.setExpenseExpenseStatusName(expense.getFkExpenseExpenseStatus().getExpenseStatusName());

            expenseResponse.setReasonForRejectExpense(expense.getReasonForRejectExpense());
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
    private String url;

    @Override
    public void saveExpenseProof(Long proofTypeId, Long expenseId, MultipartFile file) throws IOException {

        Expense expense = expenseRepository.findExpensesById(expenseId);

        EmployeeTravelPlan employeeTravelPlan =  expense.getFkEmployeeTravelPlan();

        if(employeeTravelPlan == null || employeeTravelPlan.getEmployeeIsDeletedFromTravel() ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employee travel plan not found or you are deleted from this travel.");
        }

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be add expense proof.");
        }

        LocalDate travelEndDate = travelPlan.getTravelPlanStartDate();
        LocalDate travelAddDeadLine = travelEndDate.plusDays(10);

        if(!LocalDate.now().isBefore(travelAddDeadLine) && !LocalDate.now().isAfter(travelEndDate)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only add expense proof between travel plan end date and after ending travel plan 10 days duration.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be update expense status.");
        }

        Employee user = authService.getLoginUser();
        if(user != travelPlan.getFkTravelPlanHREmployee() && user.getFkRole().getId() != 4){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "travel plan owner only update travel plan expense status.");
        }

        if(!reason.isEmpty())expense.setReasonForRejectExpense(reason);
        expense.setFkExpenseExpenseStatus(expenseStatusRepository.findExpenseStatusById(statusId));
        expense.setExpenseStatusChangeAt(Instant.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        expense.setExpenseStatusChangeBy(email);
        expenseRepository.save(expense);

        String link = "http://localhost:5173/travel-plan?travelPlanId=" + travelPlan.getId();
        String htmlMessage = "<html>" +
                "<body>" +
                "<p><strong>Travel Plan Name:</strong> " + travelPlan.getTravelPlanName() + "</p>" +
                "<p><strong>Travel Plan Start Date:</strong> " + travelPlan.getTravelPlanStartDate() + "</p>" +
                "<p><strong>Travel Plan Details:</strong> " + travelPlan.getTravelPlanDetails() + "</p>" +
                "<p><strong>Status:</strong> " + expenseStatusRepository.findExpenseStatusById(statusId).getExpenseStatusName() + "</p>" +
                "<a href=\"" + link + "\">View Travel Plan</a>" +
                "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                "</body>" +
                "</html>";
        notificationService.createNotification(expense.getFkEmployeeTravelPlan().getFkEmployee().getId()
                ,"Expense Status Upgraded by " + expense.getFkEmployeeTravelPlan().getFkTravelPlan().getFkTravelPlanHREmployee().getEmployeeEmail()
                , htmlMessage
        );
    }

    @Override
    public void saveExpenseWithProof(@Valid ExpenseRequest expenseRequest, List<MultipartFile> files, List<Long> proofTypeId) throws IOException {

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(expenseRequest.getFkEmployeeTravelPlanId());

        Integer dailyLimit = employeeTravelPlan.getFkTravelPlan().getTravelMaxExpenseAmountPerDay();

        Integer alreadySpent = getTotalExpenseByDate(employeeTravelPlan.getFkTravelPlan().getId(),
                employeeTravelPlan.getFkEmployee().getId(),
                expenseRequest.getExpenseDate());

        int remainingAllowance = dailyLimit - alreadySpent - expenseRequest.getExpenseAmount();

        if(remainingAllowance < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expense daily limit reached.");
        }

        if(Boolean.TRUE.equals(employeeTravelPlan.getEmployeeIsDeletedFromTravel())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employee travel plan not found or you are deleted from this travel.");
        }

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be be add expenses.");
        }

        LocalDate travelStartDate = travelPlan.getTravelPlanStartDate();
        LocalDate travelEndDate = travelPlan.getTravelPlanEndDate();
        LocalDate travelAddDeadLine = travelEndDate.plusDays(10);

        if(!LocalDate.now().isBefore(travelAddDeadLine) && !LocalDate.now().isAfter(travelStartDate)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only add expense with proof between travel plan start date and after ending travel plan 10 days duration.");
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
            expenseProof.setExpenseProofUrl(url + filePath);

            expenseProofRepository.save(expenseProof);

        }
        String link = "http://localhost:5173/travel-plan?travelPlanId=" + travelPlan.getId();
        String htmlMessage = "<html>" +
                "<body>" +
                "<p><strong>Travel Plan Name:</strong> " + travelPlan.getTravelPlanName() + "</p>" +
                "<p><strong>Travel Plan Start Date:</strong> " + travelPlan.getTravelPlanStartDate() + "</p>" +
                "<p><strong>Travel Plan Details:</strong> " + travelPlan.getTravelPlanDetails() + "</p>" +
                "<a href=\"" + link + "\">View Travel Plan</a>" +
                "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                "</body>" +
                "</html>";
        notificationService.createNotification(employeeTravelPlan.getFkTravelPlan().getFkTravelPlanHREmployee().getId()
                ,"Expense Added by " + employeeTravelPlan.getFkEmployee().getEmployeeEmail()
                , htmlMessage
        );

    }

    @Override
    public Integer getTotalExpenseByDate(Long travelPlanId, Long empId, LocalDate date){
        return expenseRepository.getTotalSpentByDate(travelPlanId, empId, date);
    }

}
