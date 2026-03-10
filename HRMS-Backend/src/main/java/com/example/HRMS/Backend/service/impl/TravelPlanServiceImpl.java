package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TravelPlanServiceImpl implements TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;

    private final ModelMapper modelMapper;

    private final TravelDocRepository travelDocRepository;

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;

    private final AuditTravelPlanRepository auditTravelPlanRepository;

    private final ExpenseProofTypeRepository expenseProofTypeRepository;

    private final EmployeeRepository employeeRepository;

    private final BookingParticipantRepository bookingParticipantRepository;

    private final TravelDocsTypeRepository travelDocsTypeRepository;

    private final EmailService emailService;

    private final EmployeeGameInterestRepository employeeGameInterestRepository;

    private final NotificationService notificationService;

    private final WaitlistRepository waitlistRepository;

    private final GameBookingRepository gameBookingRepository;

    private final GameBookingStatusRepository gameBookingStatusRepository;

    private final GameBookingService gameBookingService;

    public Employee getLoginUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    @Override
    public void addTravelPlan(TravelPlanRequest travelPlanRequest){

        TravelPlan travelPlan = modelMapper.map(travelPlanRequest, TravelPlan.class);

        travelPlan.setId(null);

        TravelPlan savedTravelplan = travelPlanRepository.save(travelPlan);

        List<Long> empId = travelPlanRequest.getEmployeesInTravelPlanId();
        LocalDate start = travelPlanRequest.getTravelPlanStartDate();
        LocalDate end = travelPlanRequest.getTravelPlanEndDate();

        List<GameBooking> conflicts = gameBookingRepository.findOverlappingBookings(empId, start, end);
        removeConflictGameBookings(conflicts, travelPlanRequest.getTravelPlanDetails());

        List<BookingWaitingList> waitConflicts = waitlistRepository.findOverlappingWaitlists(empId, start, end);
        removeConflictWaitingListBookings(waitConflicts, travelPlanRequest.getTravelPlanDetails());

        List<String> emails = new ArrayList<>();

        for(Long id : empId){
            Employee employee = employeeRepository.findById(id).orElseThrow(
                    () -> new RuntimeException("Employee not found"));

            if(isEmpAvailable(id, travelPlanRequest.getTravelPlanStartDate(), travelPlanRequest.getTravelPlanEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emp is not available at this travel period");
            }

            if(Objects.equals(id, travelPlanRequest.getFkTravelPlanHREmployeeId()))
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "owner can not add it self in travel members.");
            }

            EmployeeTravelPlan employeeTravelPlan = new EmployeeTravelPlan();
            employeeTravelPlan.setFkEmployee(employee);
            employeeTravelPlan.setEmployeeTravelPlanCreatedAt(Instant.now());
            employeeTravelPlan.setFkTravelPlan(savedTravelplan);
            employeeTravelPlanRepository.save(employeeTravelPlan);

            emails.add(employee.getEmployeeEmail());
            String link = "http://localhost:5173/travel-plan?travelPlanId=" + savedTravelplan.getId();
            String htmlMessage = "<html>" +
                    "<body>" +
                    "<p><strong>Travel Plan Name:</strong> " + travelPlanRequest.getTravelPlanName() + "</p>" +
                    "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                    "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                    "<a href=\"" + link + "\">View Travel Plan</a>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                    "</body>" +
                    "</html>";
            notificationService.createNotification(id,
                    "you are added in Travel Plan by Hr " + savedTravelplan.getFkTravelPlanHREmployee().getEmployeeEmail(),
                    htmlMessage);

        }

        String htmlMessage = "<html>" +
                "<body>" +
                "<h3>you are added in Travel Plan </h3>" +
                "<p><strong>By:</strong> " + savedTravelplan.getFkTravelPlanHREmployee().getEmployeeEmail() + "</p>" +
                "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                "<p><strong>Travel Plan End Date:</strong> " + travelPlanRequest.getTravelPlanEndDate() + "</p>" +
                "<p><strong>Travel Plan From:</strong> " + travelPlanRequest.getTravelPlanFrom() +
                " <strong>To:</strong> " + travelPlanRequest.getTravelPlanTo() + "</p>" +
                "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                "<p>This is an automated notification mail.</p>" +
                "</body>" +
                "</html>";
        emailService.sendEmail(emails,
                "you are added in Travel Plan: " + travelPlanRequest.getTravelPlanName() ,htmlMessage);
    }

    @Override
    public void removeConflictWaitingListBookings(List<BookingWaitingList> conflicts, String details){
        for (BookingWaitingList wait : conflicts) {

            List<String> emails = new ArrayList<>();
            emails.add(wait.getFkHostEmployee().getEmployeeEmail());

            List<BookingParticipant> bookingParticipants = bookingParticipantRepository.findByFkBookingWaitingList_Id(wait.getId());
            for(BookingParticipant bookingParticipant : bookingParticipants){
                emails.add(bookingParticipant.getFkEmployee().getEmployeeEmail());
                String htmlMessage = "<html>" +
                        "<body>" +
                        "<p><strong>Game Name:</strong> " + wait.getFkGameType().getGameName() + "</p>" +
                        "<p><strong>Slot Time:</strong> " + wait.getTargetSlotDatetime() + "</p>" +
                        "<p><strong>Game Details:</strong> " + details + "</p>" +
                        "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                        "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                        "</body>" +
                        "</html>";
                notificationService.createNotification(bookingParticipant.getFkEmployee().getId()
                        ,"your game booking waiting list entry is removed by HR"
                        ,htmlMessage);
            }
            bookingParticipantRepository.deleteAll(bookingParticipants);
            waitlistRepository.delete(wait);

            String htmlMessage = "<html>" +
                    "<body>" +
                    "<p><strong>Game Name:</strong> " + wait.getFkGameType().getGameName() + "</p>" +
                    "<p><strong>Slot Time:</strong> " + wait.getTargetSlotDatetime() + "</p>" +
                    "<p><strong>Game Details:</strong> " + details + "</p>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                    "</body>" +
                    "</html>";
            notificationService.createNotification(wait.getFkHostEmployee().getId()
                    ,"your game booking waiting list entry is removed by HR"
                    ,htmlMessage);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String hrEmail = authentication.getName();
            String htmlEmailMessage = "<html>" +
                    "<body>" +
                    "<h3>your " + wait.getFkGameType().getGameName() + " Game waiting list entry is removed </h3>" +
                    "<p><strong>By:</strong> " + hrEmail + "</p>" +
                    "<p><strong>Game Name:</strong> " + wait.getFkGameType().getGameName() + "</p>" +
                    "<p><strong>Slot Time:</strong> " + wait.getTargetSlotDatetime() + "</p>" +
                    "<p><strong>Game Details:</strong> " + details + "</p>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p>This is an automated notification mail.</p>" +
                    "</body>" +
                    "</html>";
            emailService.sendEmail(emails,"Your " + wait.getFkGameType().getGameName() + " Game waiting list entry is removed by Hr",htmlEmailMessage);

        }
    }

    @Override
    public void removeConflictGameBookings(List<GameBooking> conflicts, String details){
        for (GameBooking booking : conflicts) {

            List<String> emails = new ArrayList<>();
            emails.add(booking.getFkHostEmployee().getEmployeeEmail());

            GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(3);
            booking.setFkGameBookingStatus(gameBookingStatus);

            List<BookingParticipant> bookingParticipants = bookingParticipantRepository.findByFkGameBooking_Id(booking.getId());
            for(BookingParticipant bookingParticipant : bookingParticipants){

                EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                        (bookingParticipant.getFkEmployee().getId(), booking.getFkGameType().getId());

                if (employeeGameInterest != null) {
                    employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle() - 1);
                    employeeGameInterestRepository.save(employeeGameInterest);
                }

                emails.add(bookingParticipant.getFkEmployee().getEmployeeEmail());
                String htmlMessage = "<html>" +
                        "<body>" +
                        "<p><strong>Game Name:</strong> " + booking.getFkGameType().getGameName() + "</p>" +
                        "<p><strong>Slot Time:</strong> " + booking.getGameBookingStartTime() + "</p>" +
                        "<p><strong>Game Details:</strong> " + details + "</p>" +
                        "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                        "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                        "</body>" +
                        "</html>";
                notificationService.createNotification(bookingParticipant.getFkEmployee().getId()
                        ,"your game booking is removed by HR"
                        ,htmlMessage);
                bookingParticipantRepository.delete(bookingParticipant);
            }

            EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                    (booking.getFkHostEmployee().getId(), booking.getFkGameType().getId());

            if (employeeGameInterest != null) {
                employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle() - 1);
                employeeGameInterestRepository.save(employeeGameInterest);
            }

            if (booking.getFkGameBookingStatus().getId() == 1 ) {
                gameBookingService.updateWaitingList(booking.getFkGameType(), booking.getGameBookingStartTime());
            }
            String htmlMessage = "<html>" +
                    "<body>" +
                    "<p><strong>Game Name:</strong> " + booking.getFkGameType().getGameName() + "</p>" +
                    "<p><strong>Slot Time:</strong> " + booking.getGameBookingStartTime() + "</p>" +
                    "<p><strong>Game Details:</strong> " + details + "</p>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                    "</body>" +
                    "</html>";
            notificationService.createNotification(booking.getFkHostEmployee().getId()
                    ,"your game booking is removed by HR"
                    , htmlMessage);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String hrEmail = authentication.getName();
            String htmlEmailMessage = "<html>" +
                    "<body>" +
                    "<h3>your " + booking.getFkGameType().getGameName() + " Game booking entry is removed </h3>" +
                    "<p><strong>By:</strong> " + hrEmail + "</p>" +
                    "<p><strong>Game Name:</strong> " + booking.getFkGameType().getGameName() + "</p>" +
                    "<p><strong>Slot Time:</strong> " + booking.getGameBookingStartTime() + "</p>" +
                    "<p><strong>Game Details:</strong> " + details + "</p>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p>This is an automated notification mail.</p>" +
                    "</body>" +
                    "</html>";
            emailService.sendEmail(emails,"Your " + booking.getFkGameType().getGameName() + " Game Booking entry is removed by Hr",htmlEmailMessage);
        }

        gameBookingRepository.saveAll(conflicts);
    }

    @Transactional
    @Override
    public void updateTravelPlan(@Valid TravelPlanRequest travelPlanRequest, Long travelPlanId){

        if(Boolean.TRUE.equals(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanIsDeleted())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be edit.");
        }

        if(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only edit travel plan before it start.");
        }

        Employee user = getLoginUser();
        Long hrEmpId = travelPlanRequest.getFkTravelPlanHREmployeeId();
        if(user != employeeRepository.findEmployeeById(hrEmpId) && user.getFkRole().getId() != 4){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "travel plan owner only update travel plan.");
        }

        List<Long> existingEmployeesId = employeeTravelPlanRepository.findEmployeeIdByTravelPlanId(travelPlanId);

        TravelPlan travelPlan = modelMapper.map(travelPlanRequest, TravelPlan.class);

        travelPlan.setId(travelPlanId);

        TravelPlan savedTravelplan = travelPlanRepository.save(travelPlan);

        List<Long> newEmpId = travelPlanRequest.getEmployeesInTravelPlanId();
        LocalDate start = travelPlanRequest.getTravelPlanStartDate();
        LocalDate end = travelPlanRequest.getTravelPlanEndDate();

        List<GameBooking> conflicts = gameBookingRepository.findOverlappingBookings(newEmpId, start, end);
        removeConflictGameBookings(conflicts, travelPlanRequest.getTravelPlanDetails());

        List<BookingWaitingList> waitConflicts = waitlistRepository.findOverlappingWaitlists(newEmpId, start, end);
        removeConflictWaitingListBookings(waitConflicts, travelPlanRequest.getTravelPlanDetails());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee hrOwner = employeeRepository.findEmployeeById(travelPlanRequest.getFkTravelPlanHREmployeeId());
        AuditTravelPlan log = new AuditTravelPlan();
        log.setAction("Edit");
        log.setPerformedBy(email);
        log.setOwnerEmail(hrOwner.getEmployeeEmail());

        List<String> addedTravelMembers = new ArrayList<>();
        List<String> removedTravelMembers = new ArrayList<>();
        List<String> emails = new ArrayList<>();

        for(Long id : newEmpId){
            Employee employee = employeeRepository.findEmployeeById(id);

            if(!existingEmployeesId.contains(id)) {
                Long employeeTravelPlanId = employeeTravelPlanRepository.findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(id,travelPlanId);

                if(employeeTravelPlanId != null){
                    EmployeeTravelPlan employeeTravelPlan1 = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);

                    if(isEmpAvailable(id, travelPlanRequest.getTravelPlanStartDate(), travelPlanRequest.getTravelPlanEndDate())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emp is not available at this travel period");
                    }

                    employeeTravelPlan1.setEmployeeIsDeletedFromTravel(false);

                    employeeTravelPlanRepository.save(employeeTravelPlan1);

                    travelDocRepository.findByFkEmployeeAndFkTravelPlanAndReAddIt(employee, travelPlan);

                    travelDocRepository.findByFkEmployeeAndFkEmployeeTravelPlanAndReAddIt(employee, employeeTravelPlan1);

                    List<String> emails1 = new ArrayList<>();
                    emails1.add(employee.getEmployeeEmail());
                    addedTravelMembers.add(employee.getEmployeeEmail());
                    String htmlEmailMessage = "<html>" +
                            "<body>" +
                            "<h3>you are readded in Travel Plan </h3>" +
                            "<p><strong>By:</strong> " + savedTravelplan.getFkTravelPlanHREmployee().getEmployeeEmail() + "</p>" +
                            "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                            "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                            "<p><strong>Travel Plan End Date:</strong> " + travelPlanRequest.getTravelPlanEndDate() + "</p>" +
                            "<p><strong>Travel Plan From:</strong> " + travelPlanRequest.getTravelPlanFrom() +
                            " <strong>To:</strong> " + travelPlanRequest.getTravelPlanTo() + "</p>" +
                            "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                            "<p>This is an automated notification mail.</p>" +
                            "</body>" +
                            "</html>";
                    emailService.sendEmail(emails1,"You are reAdded in Travel Plan " + travelPlanRequest.getTravelPlanName() + " by Hr",htmlEmailMessage);

                    String link = "http://localhost:5173/travel-plan?travelPlanId=" + savedTravelplan.getId();
                    String htmlMessage = "<html>" +
                            "<body>" +
                            "<p><strong>Travel Plan Name:</strong> " + travelPlanRequest.getTravelPlanName() + "</p>" +
                            "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                            "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                            "<a href=\"" + link + "\">View Travel Plan</a>" +
                            "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                            "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                            "</body>" +
                            "</html>";
                    notificationService.createNotification(id,
                            "You are reAdded in Travel Plan by HR",
                            htmlMessage);

                } else {

                    if(isEmpAvailable(id, travelPlanRequest.getTravelPlanStartDate(), travelPlanRequest.getTravelPlanEndDate())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emp is not available at this travel period");
                    }

                    EmployeeTravelPlan employeeTravelPlan = new EmployeeTravelPlan();
                    employeeTravelPlan.setFkEmployee(employee);
                    employeeTravelPlan.setEmployeeTravelPlanCreatedAt(Instant.now());
                    employeeTravelPlan.setFkTravelPlan(savedTravelplan);
                    employeeTravelPlanRepository.save(employeeTravelPlan);

                    emails.add(employee.getEmployeeEmail());
                    String link = "http://localhost:5173/travel-plan?travelPlanId=" + savedTravelplan.getId();
                    String htmlMessage = "<html>" +
                            "<body>" +
                            "<p><strong>Travel Plan Name:</strong> " + travelPlanRequest.getTravelPlanName() + "</p>" +
                            "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                            "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                            "<a href=\"" + link + "\">View Travel Plan</a>" +
                            "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                            "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                            "</body>" +
                            "</html>";
                    addedTravelMembers.add(employee.getEmployeeEmail());
                    notificationService.createNotification(id,
                            "You are added in Travel Plan by Hr",
                            htmlMessage);

                }
            }

        }

        for(Long id : existingEmployeesId){

            if(!newEmpId.contains(id)) {

                markEmployeeTravelPlanAsDelete(id,travelPlanId);
                Employee employee = employeeRepository.findEmployeeById(id);
                List<String> emails1 = new ArrayList<>();
                emails1.add(employee.getEmployeeEmail());
                removedTravelMembers.add(employee.getEmployeeEmail());

                String htmlEmailMessage = "<html>" +
                        "<body>" +
                        "<h3>you are removed from Travel Plan </h3>" +
                        "<p><strong>By:</strong> " + savedTravelplan.getFkTravelPlanHREmployee().getEmployeeEmail() + "</p>" +
                        "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                        "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                        "<p><strong>Travel Plan End Date:</strong> " + travelPlanRequest.getTravelPlanEndDate() + "</p>" +
                        "<p><strong>Travel Plan From:</strong> " + travelPlanRequest.getTravelPlanFrom() +
                        " <strong>To:</strong> " + travelPlanRequest.getTravelPlanTo() + "</p>" +
                        "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                        "<p>This is an automated notification mail.</p>" +
                        "</body>" +
                        "</html>";
                emailService.sendEmail(emails1,"You are Removed from Travel Plan " + travelPlanRequest.getTravelPlanName() + " by Hr",htmlEmailMessage);

                String link = "http://localhost:5173/travel-plan?travelPlanId=" + savedTravelplan.getId();
                String htmlMessage = "<html>" +
                        "<body>" +
                        "<p><strong>Travel Plan Name:</strong> " + travelPlanRequest.getTravelPlanName() + "</p>" +
                        "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                        "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                        "<a href=\"" + link + "\">View Travel Plan</a>" +
                        "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                        "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                        "</body>" +
                        "</html>";
                notificationService.createNotification(id,
                        "You are Removed from Travel Plan by HR",
                        htmlMessage);

            }
        }

        if(!emails.isEmpty()){
            String htmlEmailMessage = "<html>" +
                    "<body>" +
                    "<h3>you are added in Travel Plan </h3>" +
                    "<p><strong>By:</strong> " + savedTravelplan.getFkTravelPlanHREmployee().getEmployeeEmail() + "</p>" +
                    "<p><strong>Travel Plan Details:</strong> " + travelPlanRequest.getTravelPlanDetails() + "</p>" +
                    "<p><strong>Travel Plan Start Date:</strong> " + travelPlanRequest.getTravelPlanStartDate() + "</p>" +
                    "<p><strong>Travel Plan End Date:</strong> " + travelPlanRequest.getTravelPlanEndDate() + "</p>" +
                    "<p><strong>Travel Plan From:</strong> " + travelPlanRequest.getTravelPlanFrom() +
                    " <strong>To:</strong> " + travelPlanRequest.getTravelPlanTo() + "</p>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p>This is an automated notification mail.</p>" +
                    "</body>" +
                    "</html>";
            emailService.sendEmail(emails,"You are added in Travel Plan " + travelPlanRequest.getTravelPlanName() + " by Hr",htmlEmailMessage);

        }

        log.setAddedTravelMembers(addedTravelMembers);
        log.setRemovedTravelMembers(removedTravelMembers);
        auditTravelPlanRepository.save(log);

    }

    @Transactional
    @Override
    public void markEmployeeTravelPlanAsDelete(Long empId, Long travelPlanId){

        Employee employee = employeeRepository.findEmployeeById(empId);

        Long employeeTravelPlanId = employeeTravelPlanRepository.findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(empId,travelPlanId);

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);

        employeeTravelPlan.setEmployeeIsDeletedFromTravel(true);

        employeeTravelPlanRepository.save(employeeTravelPlan);

        travelDocRepository.findByFkEmployeeAndFkEmployeeTravelPlanAndRemoveIt(employee, employeeTravelPlan);

    }

    @Transactional
    @Override
    public void markAsDeleted(Long travelPlanId, String reason){

        if(Boolean.TRUE.equals(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanIsDeleted())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be mark as deleted.");
        }

        if(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only delete travel plan before it start.");
        }

        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelPlanId);

        Employee user = getLoginUser();
        if(user != travelPlan.getFkTravelPlanHREmployee() && user.getFkRole().getId() != 4){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "travel plan owner only delete travel plan.");
        }

        travelPlan.setTravelPlanIsDeleted(true);
        travelPlan.setReasonForDeleteTravelPlan(reason);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        travelPlan.setTravelPlanDeletedBy(email);
        travelPlan.setTravelPlanDeletedAt(Instant.now());
        travelPlanRepository.save(travelPlan);

        Long hrId = travelPlan.getFkTravelPlanHREmployee().getId();
        Employee hrEmployee = employeeRepository.findEmployeeById(hrId);
        travelDocRepository.findByFkEmployeeAndFkTravelPlanAndRemoveIt(hrEmployee, travelPlan);

        Employee employee = employeeRepository.findEmployeeById(hrId);
        List<String> emails = new ArrayList<>();
        emails.add(employee.getEmployeeEmail());

        List<Long> empIds = employeeTravelPlanRepository.findEmployeeIdByTravelPlanId(travelPlanId);

        for(Long empId: empIds) {
            markEmployeeTravelPlanAsDelete(empId, travelPlanId);
            Employee employee1 = employeeRepository.findEmployeeById(empId);
            List<String> emails1 = new ArrayList<>();
            emails1.add(employee1.getEmployeeEmail());

            String htmlEmailMessage = "<html>" +
                    "<body>" +
                    "<h3>Travel Plan is deleted </h3>" +
                    "<p><strong>By:</strong> " + travelPlan.getFkTravelPlanHREmployee().getEmployeeEmail() + "</p>" +
                    "<p><strong>Travel Plan Details:</strong> " + travelPlan.getTravelPlanDetails() + "</p>" +
                    "<p><strong>Travel Plan Start Date:</strong> " + travelPlan.getTravelPlanStartDate() + "</p>" +
                    "<p><strong>Travel Plan End Date:</strong> " + travelPlan.getTravelPlanEndDate() + "</p>" +
                    "<p><strong>Travel Plan From:</strong> " + travelPlan.getTravelPlanFrom() +
                    " <strong>To:</strong> " + travelPlan.getTravelPlanTo() + "</p>" +
                    "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                    "<p>This is an automated notification mail.</p>" +
                    "</body>" +
                    "</html>";
            emailService.sendEmail(emails1,"Travel Plan " + travelPlan.getTravelPlanName() + " is deleted by Hr",htmlEmailMessage);

            String link = "http://localhost:5173/travel-plan?travelPlanId=" + travelPlanId;
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
            notificationService.createNotification(empId,
                    "Travel Plan deleted by HR",
                    htmlMessage);
        }

        String htmlEmailMessage = "<html>" +
                "<body>" +
                "<h3>Travel Plan is deleted</h3>" +
                "<p><strong>By You:</strong> " + travelPlan.getFkTravelPlanHREmployee().getEmployeeEmail() + "</p>" +
                "<p><strong>Travel Plan Details:</strong> " + travelPlan.getTravelPlanDetails() + "</p>" +
                "<p><strong>Travel Plan Start Date:</strong> " + travelPlan.getTravelPlanStartDate() + "</p>" +
                "<p><strong>Travel Plan End Date:</strong> " + travelPlan.getTravelPlanEndDate() + "</p>" +
                "<p><strong>Travel Plan From:</strong> " + travelPlan.getTravelPlanFrom() +
                    " <strong>To:</strong> " + travelPlan.getTravelPlanTo() + "</p>" +
                "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                "<p>This is an automated notification mail.</p>" +
                "</body>" +
                "</html>";
        emailService.sendEmail(emails,"Travel Plan " + travelPlan.getTravelPlanName() + " is deleted by you",htmlEmailMessage);

        String link = "http://localhost:5173/travel-plan?travelPlanId=" + travelPlanId;
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
        notificationService.createNotification(hrId,
                "Travel Plan is deleted by You",
                htmlMessage);
    }


    @Override
    public TravelPlanResponse showTravelPlanById(Long travelplanId){
        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelplanId);
        TravelPlanResponse travelPlanResponse = modelMapper.map(travelPlan,TravelPlanResponse.class);

        List<EmployeeTravelPlanResponse> employeeTravelPlanResponses = new ArrayList<>();
        for(EmployeeTravelPlan employeeTravelPlan:employeeTravelPlanRepository.findEmployeeTravelPlanByFkTravelPlan_Id(travelPlan.getId()))
        {
            EmployeeTravelPlanResponse employeeTravelPlanResponse = modelMapper.map(employeeTravelPlan, EmployeeTravelPlanResponse.class);
            employeeTravelPlanResponses.add(employeeTravelPlanResponse);
        }
        travelPlanResponse.setEmployeeTravelPlanResponses(employeeTravelPlanResponses);

        return travelPlanResponse;
    }

    @Override
    public Page<TravelPlanResponse> showAllTravelPlan(String searchTerm, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPlan> travelPlans = travelPlanRepository.findTravelPlans(searchTerm, pageable);
        return travelPlans.map(travelPlan -> {
            TravelPlanResponse travelPlanResponse = modelMapper.map(travelPlan, TravelPlanResponse.class);

            List<EmployeeTravelPlanResponse> employeeTravelPlanResponses = new ArrayList<>();
            for (EmployeeTravelPlan employeeTravelPlan : employeeTravelPlanRepository.findEmployeeTravelPlanByFkTravelPlan_Id(travelPlan.getId())) {
                EmployeeTravelPlanResponse employeeTravelPlanResponse = modelMapper.map(employeeTravelPlan, EmployeeTravelPlanResponse.class);
                employeeTravelPlanResponses.add(employeeTravelPlanResponse);
            }
            travelPlanResponse.setEmployeeTravelPlanResponses(employeeTravelPlanResponses);

            return travelPlanResponse;
        });
    }


    @Override
    public Page<TravelPlanResponse> findTravelPlanByEmployeeId(Long empId, String searchTerm, Long travelPlanType, int page, int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPlan> travelPlans = employeeTravelPlanRepository.findTravelPlanByFkEmployee_Id(empId, searchTerm, travelPlanType, pageable);

        return travelPlans.map(travelPlan -> {
            TravelPlanResponse travelPlanResponse = modelMapper.map(travelPlan,TravelPlanResponse.class);

            List<EmployeeTravelPlanResponse> employeeTravelPlanResponses = new ArrayList<>();
            for(EmployeeTravelPlan employeeTravelPlan:employeeTravelPlanRepository.findEmployeeTravelPlanByFkTravelPlan_Id(travelPlan.getId()))
            {
                EmployeeTravelPlanResponse employeeTravelPlanResponse = modelMapper.map(employeeTravelPlan, EmployeeTravelPlanResponse.class);
                employeeTravelPlanResponses.add(employeeTravelPlanResponse);
            }
            travelPlanResponse.setEmployeeTravelPlanResponses(employeeTravelPlanResponses);
            return travelPlanResponse;
        });
    }

    @Value("${img.path}")
    private String folderPath;

    @Value("${URL.path}")
    private String url;

    @Override
    public void saveDocByEmployee(Long employeeTravelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException {

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);

        if(employeeTravelPlan == null){
            throw new RemoteException("employeeTravelPlan not found");
        }

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be add docs.");
        }

        if(travelPlan.getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only add docs before travel plan start.");
        }

        Employee employee = employeeRepository.findEmployeeById(employeeId);
        Long empId = employee.getId();

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "travel_doc/" + employeeTravelPlanId +"_" + docTypeId + "_" + empId  + "_" + originalFilePath;

        file.transferTo(new File(System.getProperty("user.dir") + '/' +folderPath + filePath));

        TravelDoc travelDoc = new TravelDoc();
        travelDoc.setFkEmployee(employeeRepository.findEmployeeById(empId));

        travelDoc.setFkEmployeeTravelPlan(employeeTravelPlan);
        travelDoc.setTravelDocUrl(url + filePath);
        travelDoc.setTravelDocUploadedAt(Instant.now());
        travelDoc.setFkTravelDocsType(travelDocsTypeRepository.findTravelDocsTypeById(docTypeId));

        travelDocRepository.save(travelDoc);
    }

    @Override
    public void saveDocByHr(Long travelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException {

        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelPlanId);

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "closed travel plan cannot be add docs.");
        }

        if(travelPlan.getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only add docs before travel plan start.");
        }

        Employee user = getLoginUser();

        if(user != travelPlan.getFkTravelPlanHREmployee() && !employeeTravelPlanRepository.isEmployeeTravelPlanByEmployeeIdAndTravelPlanIdExist(user.getId(), travelPlanId)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "you are not in this travel plan member or not an owner so you cannot add docs.");
            }
        
        Employee employee = employeeRepository.findEmployeeById(employeeId);
        Long empId = employee.getId();

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "travel_doc/" + travelPlanId +"_" + docTypeId + "_" + empId  + "_" + originalFilePath;

        file.transferTo(new File(System.getProperty("user.dir") + '/' +folderPath + filePath));

        TravelDoc travelDoc = new TravelDoc();
        travelDoc.setFkEmployee(employeeRepository.findEmployeeById(empId));
        if(travelPlanRepository.findTravelPlanById(travelPlanId) == null){
            throw new RemoteException("travel plan not found");
        }
        travelDoc.setFkTravelPlan(travelPlanRepository.findTravelPlanById(travelPlanId));
        travelDoc.setTravelDocUrl(url + filePath);
        travelDoc.setTravelDocUploadedAt(Instant.now());
        travelDoc.setFkTravelDocsType(travelDocsTypeRepository.findTravelDocsTypeById(docTypeId));

        travelDocRepository.save(travelDoc);
    }

    @Override
    public List<TravelDocResponse> findAllTravelDoc() throws IOException {

        List<TravelDoc> travelDocs = travelDocRepository.findAll();
        List<TravelDocResponse> travelDocResponses = new ArrayList<>();
        for(TravelDoc travelDoc : travelDocs)
        {
            TravelDocResponse travelDocResponse = modelMapper.map(travelDoc,TravelDocResponse.class);
            travelDocResponses.add(travelDocResponse);
        }

        return travelDocResponses;

    }

    @Override
    public List<TravelDocResponse> findTravelDocByFkEmployeeId(Long empId, Long travelPlanId){

        Employee employee = employeeRepository.findEmployeeById(empId);
        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelPlanId);
        List<TravelDoc> travelDocs = travelDocRepository.findByFkEmployeeAndFkTravelPlan(employee ,travelPlan);

        Long employeeTravelPlanId = employeeTravelPlanRepository.findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(empId, travelPlanId);
        List<TravelDoc> travelDocsEmp = travelDocRepository.findTravelDocsByFkEmployeeTravelPlan_Id(employeeTravelPlanId);
        travelDocs.addAll(travelDocsEmp);

        List<TravelDocResponse> travelDocResponses = new ArrayList<>();
        for(TravelDoc travelDoc : travelDocs)
        {
            TravelDocResponse travelDocResponse = modelMapper.map(travelDoc,TravelDocResponse.class);
            travelDocResponses.add(travelDocResponse);
        }

        return travelDocResponses;
    }

    @Override
    public List<TravelDocResponse> findAllTravelPlanDocByTravelPlan(Long travelPlanId, Long empId, String searchTerm) {
        Employee employee = employeeRepository.findEmployeeById(empId);
        Long roleId = employee.getFkRole().getId();

        List<TravelDoc> travelDocs = travelDocRepository.findTravelDocs(
                travelPlanId, empId, roleId, searchTerm
        );

        return travelDocs.stream()
                .map(doc -> modelMapper.map(doc, TravelDocResponse.class))
                .toList();
    }


    @Override
    public Long findEmployeeTravelPlanId(Long empId, Long travelId){
        Employee employee = employeeRepository.findEmployeeById(empId);
        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelId);
        return travelPlanRepository.findEmployeeTravelPlanId(employee,travelPlan);
    }

    @Override
    public List<Long> getTravelPlan(String query){
        return travelPlanRepository.findTravelPlan(query);
    }

    @Override
    public List<TravelDocsType> getAllDocTypes(){
        return travelDocsTypeRepository.findAll();
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoNotificationForExpenseAdd() {
        LocalDate now = LocalDate.now().minusDays(5);

        List<TravelPlan> travelPlans = travelPlanRepository
                .findAllByTravelPlanEndDateAndTravelPlanIsDeleted(now, false);

        if (!travelPlans.isEmpty()) {
            for(TravelPlan travelPlan : travelPlans) {
                List<Long> employeeIdByTravelPlanId = employeeTravelPlanRepository.findEmployeeIdByTravelPlanId(travelPlan.getId());

                String link = "http://localhost:5173/travel-plan?travelPlanId=" + travelPlan.getId();
                for (Long id : employeeIdByTravelPlanId) {
                    String htmlMessage = "<html>" +
                            "<body>" +
                            "<p><strong>Message:</strong> You have now only 5 days remaining to add expense </p>" +
                            "<p><strong>Travel Plan Name:</strong> " + travelPlan.getTravelPlanName() + "</p>" +
                            "<p><strong>Travel Plan Start Date:</strong> " + travelPlan.getTravelPlanStartDate() + "</p>" +
                            "<p><strong>Travel Plan Details:</strong> " + travelPlan.getTravelPlanDetails() + "</p>" +
                            "<a href=\"" + link + "\">View Travel Plan</a>" +
                            "<p><strong>Date:</strong> " + LocalDateTime.now().toLocalDate() + "</p>" +
                            "<p><strong>Time:</strong> " + LocalDateTime.now().toLocalTime() + "</p>" +
                            "</body>" +
                            "</html>";
                    notificationService.createNotification(id
                            , "Expense Upload reminder"
                            , htmlMessage
                    );
                }
            }

        }

    }

    @Override
    public boolean isEmpAvailable(Long id, LocalDate startDate, LocalDate endDate){
        return travelPlanRepository.findAllByTravelStartTimeBetween(
                id, startDate, endDate
        );
    }

    @Override
    public List<ExpenseProofType> showAllExpenseTypes(){
        return expenseProofTypeRepository.findAll();
    }
}
