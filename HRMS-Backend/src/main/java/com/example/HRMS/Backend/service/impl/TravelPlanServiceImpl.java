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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hibernate.type.descriptor.java.CoercionHelper.toLong;

@Service
@RequiredArgsConstructor
public class TravelPlanServiceImpl implements TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;

    private final ModelMapper modelMapper;

    private final TravelDocRepository travelDocRepository;

    private final ExpenseStatusRepository expenseStatusRepository;

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;

    private final EmployeeRepository employeeRepository;

    private final BookingParticipantRepository bookingParticipantRepository;

    private final TravelDocsTypeRepository travelDocsTypeRepository;

    private final EmailService emailService;

    private final EmployeeGameInterestRepository employeeGameInterestRepository;

    private final NotificationService notificationService;

    private final WaitlistRepository waitlistRepository;

    private final GameBookingRepository gameBookingRepository;

    private final GameBookingStatusRepository gameBookingStatusRepository;

    private final NotificationRepository notificationRepository;

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
                throw new RuntimeException("emp is not available at this travel period");
            }

            EmployeeTravelPlan employeeTravelPlan = new EmployeeTravelPlan();
            employeeTravelPlan.setFkEmployee(employee);
            employeeTravelPlan.setEmployeeTravelPlanCreatedAt(Instant.now());
            employeeTravelPlan.setFkTravelPlan(savedTravelplan);
            employeeTravelPlanRepository.save(employeeTravelPlan);

            emails.add(employee.getEmployeeEmail());
            notificationService.createNotification(id,"you are added in Travel Plan by Hr at :" + Instant.now(),travelPlanRequest.getTravelPlanDetails());

        }

        emailService.sendEmail(emails,"Travel Plan",travelPlanRequest.getTravelPlanDetails());
    }

    @Override
    public void removeConflictWaitingListBookings(List<BookingWaitingList> conflicts, String details){
        for (BookingWaitingList wait : conflicts) {

            List<String> emails = new ArrayList<>();
            emails.add(wait.getFkHostEmployee().getEmployeeEmail());

            List<BookingParticipant> bookingParticipants = bookingParticipantRepository.findByFkBookingWaitingList_Id(wait.getId());
            for(BookingParticipant bookingParticipant : bookingParticipants){
                emails.add(bookingParticipant.getFkEmployee().getEmployeeEmail());
                notificationService.createNotification(bookingParticipant.getFkEmployee().getId()
                        ,"your game booking waiting list entry is removed at :" + Instant.now()
                        ," date : " + wait.getTargetSlotDatetime() + " game type : "+ wait.getFkGameType().getGameName() +" details : " + details);
            }
            bookingParticipantRepository.deleteAll(bookingParticipants);
            waitlistRepository.delete(wait);

            notificationService.createNotification(wait.getFkHostEmployee().getId()
                    ,"your game booking waiting list entry is removed at :" + Instant.now()
                    ," date : " + wait.getTargetSlotDatetime() + " game type : "+ wait.getFkGameType().getGameName() +" details : " + details);

            emailService.sendEmail(emails,"your game booking waiting list entry is at :" + Instant.now()
                    , " date : " + wait.getTargetSlotDatetime() + " game type : "+ wait.getFkGameType().getGameName() +" details : " + details);
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
                notificationService.createNotification(bookingParticipant.getFkEmployee().getId()
                        ,"your game booking is removed at :" + Instant.now()
                        ," date : " + booking.getGameBookingStartTime() + " game type : "+ booking.getFkGameType().getGameName() +" details : " + details);
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

            notificationService.createNotification(booking.getFkHostEmployee().getId()
                    ,"your game booking is removed at :" + Instant.now()
                    ," date : " + booking.getGameBookingStartTime() + " game type : "+ booking.getFkGameType().getGameName() +" details : " + details);
            emailService.sendEmail(emails,"your game booking is removed at :" + Instant.now()
                    , " date : " + booking.getGameBookingStartTime() + " game type : "+ booking.getFkGameType().getGameName() +" details : " + details);

        }

        gameBookingRepository.saveAll(conflicts);
    }

    @Transactional
    @Override
    public void updateTravelPlan(@Valid TravelPlanRequest travelPlanRequest, Long travelPlanId){

        if(Boolean.TRUE.equals(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be edit.");
        }

        if(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new RuntimeException("only edit travel plan before it start.");
        }

        Employee user = getLoginUser();
        Long hrEmpId = travelPlanRequest.getFkTravelPlanHREmployeeId();
        if(user != employeeRepository.findEmployeeById(hrEmpId)){
            throw new RuntimeException("travel plan owner only update travel plan.");
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


        List<String> emails = new ArrayList<>();

        for(Long id : newEmpId){
            Employee employee = employeeRepository.findEmployeeById(id);

            if(!existingEmployeesId.contains(id)) {
                Long employeeTravelPlanId = employeeTravelPlanRepository.findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(id,travelPlanId);

                if(employeeTravelPlanId != null){
                    EmployeeTravelPlan employeeTravelPlan1 = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);

                    if(isEmpAvailable(id, travelPlanRequest.getTravelPlanStartDate(), travelPlanRequest.getTravelPlanEndDate())) {
                        throw new RuntimeException("emp is not available at this travel period");
                    }

                    employeeTravelPlan1.setEmployeeIsDeletedFromTravel(false);

                    employeeTravelPlanRepository.save(employeeTravelPlan1);

                    travelDocRepository.findByFkEmployeeAndFkTravelPlanAndReAddIt(employee, travelPlan);

                    travelDocRepository.findByFkEmployeeAndFkEmployeeTravelPlanAndReAddIt(employee, employeeTravelPlan1);

                    List<String> emails1 = new ArrayList<>();
                    emails1.add(employee.getEmployeeEmail());
                    emailService.sendEmail(emails1,"ReAdded from Travel Plan by Hr at :" + Instant.now(),travelPlanRequest.getTravelPlanDetails());
                    notificationService.createNotification(id,"ReAdded from Travel Plan by Hr at :" + Instant.now(),travelPlanRequest.getTravelPlanDetails());

                } else {

                    if(isEmpAvailable(id, travelPlanRequest.getTravelPlanStartDate(), travelPlanRequest.getTravelPlanEndDate())) {
                        throw new RuntimeException("emp is not available at this travel period");
                    }

                    EmployeeTravelPlan employeeTravelPlan = new EmployeeTravelPlan();
                    employeeTravelPlan.setFkEmployee(employee);
                    employeeTravelPlan.setEmployeeTravelPlanCreatedAt(Instant.now());
                    employeeTravelPlan.setFkTravelPlan(savedTravelplan);
                    employeeTravelPlanRepository.save(employeeTravelPlan);

                    emails.add(employee.getEmployeeEmail());
                    notificationService.createNotification(id,"New added in Travel Plan by Hr at :" + Instant.now(),travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanDetails());

                }
            }

        }

        for(Long id : existingEmployeesId){

            if(!newEmpId.contains(id)) {

                markEmployeeTravelPlanAsDelete(id,travelPlanId);
                Employee employee = employeeRepository.findEmployeeById(id);
                List<String> emails1 = new ArrayList<>();
                emails1.add(employee.getEmployeeEmail());
                emailService.sendEmail(emails1,"Removed from Travel Plan by Hr at :" + Instant.now(),travelPlanRequest.getTravelPlanDetails());
                notificationService.createNotification(id,"Removed from Travel Plan by Hr at :" + Instant.now(),travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanDetails());

            }
        }

        if(!emails.isEmpty()){
            emailService.sendEmail(emails,"Travel Plan",travelPlanRequest.getTravelPlanDetails());
        }
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
            throw new RuntimeException("closed travel plan cannot be mark as deleted.");
        }

        if(travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new RuntimeException("only delete travel plan before it start.");
        }

        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelPlanId);

        Employee user = getLoginUser();
        if(user != travelPlan.getFkTravelPlanHREmployee()){
            throw new RuntimeException("travel plan owner only delete travel plan.");
        }

        travelPlan.setTravelPlanIsDeleted(true);
        travelPlan.setReasonForDeleteTravelPlan(reason);
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
            emailService.sendEmail(emails1,"Travel Plan id deleted by Hr at :" + Instant.now(),travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanDetails());
            notificationService.createNotification(empId,"Travel Plan id deleted by Hr at :" + Instant.now(),travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanDetails());
        }

        emailService.sendEmail(emails,"Travel Plan id deleted by You at :" + Instant.now(),travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanDetails());
        notificationService.createNotification(hrId,"Travel Plan id deleted by You at :" + Instant.now(),travelPlanRepository.findTravelPlanById(travelPlanId).getTravelPlanDetails());
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
    public List<TravelPlanResponse> showAllTravelPlan(){
        List<TravelPlanResponse> travelPlanResponses = new ArrayList<>();
        for(TravelPlan travelPlan : travelPlanRepository.findAll()){
            TravelPlanResponse travelPlanResponse = modelMapper.map(travelPlan,TravelPlanResponse.class);

            List<EmployeeTravelPlanResponse> employeeTravelPlanResponses = new ArrayList<>();
            for(EmployeeTravelPlan employeeTravelPlan:employeeTravelPlanRepository.findEmployeeTravelPlanByFkTravelPlan_Id(travelPlan.getId()))
            {
                EmployeeTravelPlanResponse employeeTravelPlanResponse = modelMapper.map(employeeTravelPlan, EmployeeTravelPlanResponse.class);
                employeeTravelPlanResponses.add(employeeTravelPlanResponse);
            }
            travelPlanResponse.setEmployeeTravelPlanResponses(employeeTravelPlanResponses);

            travelPlanResponses.add(travelPlanResponse);
        }
        return travelPlanResponses;
    }


    @Override
    public Page<TravelPlanResponse> findTravelPlanByEmployeeId(Long empId, String searchTerm, int page, int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPlan> travelPlans = employeeTravelPlanRepository.findTravelPlanByFkEmployee_Id(empId, searchTerm, pageable);

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
    private String URL;

    @Override
    public void saveDocByEmployee(Long employeeTravelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException {

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);

        if(employeeTravelPlan == null){
            throw new RemoteException("employeeTravelPlan not found");
        }

        TravelPlan travelPlan = employeeTravelPlan.getFkTravelPlan();

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be add docs.");
        }

        if(travelPlan.getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new RuntimeException("only add docs before travel plan start.");
        }

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
//                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Employee employee = employeeRepository.findEmployeeById(employeeId);
        Long empId = employee.getId();

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "travel_doc/" + employeeTravelPlanId +"_" + docTypeId + "_" + empId  + "_" + originalFilePath;

        file.transferTo(new File(System.getProperty("user.dir") + "/" +folderPath + filePath));

        TravelDoc travelDoc = new TravelDoc();
        travelDoc.setFkEmployee(employeeRepository.findEmployeeById(empId));

        travelDoc.setFkEmployeeTravelPlan(employeeTravelPlan);
        travelDoc.setTravelDocUrl(URL + filePath);
        travelDoc.setTravelDocUploadedAt(Instant.now());
        travelDoc.setFkTravelDocsType(travelDocsTypeRepository.findTravelDocsTypeById(docTypeId));

        travelDocRepository.save(travelDoc);
    }

    @Override
    public void saveDocByHr(Long travelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException {

        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelPlanId);

        if(Boolean.TRUE.equals(travelPlan.getTravelPlanIsDeleted())){
            throw new RuntimeException("closed travel plan cannot be add docs.");
        }

        if(travelPlan.getTravelPlanStartDate().isBefore(LocalDate.now())){
            throw new RuntimeException("only add docs before travel plan start.");
        }

        Employee user = getLoginUser();

        if(user != travelPlan.getFkTravelPlanHREmployee() && !employeeTravelPlanRepository.isEmployeeTravelPlanByEmployeeIdAndTravelPlanIdExist(user.getId(), travelPlanId)){
                throw new RuntimeException("you are not in this travel plan member or not an owner so you cannot add docs.");
            }


//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
//                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Employee employee = employeeRepository.findEmployeeById(employeeId);
        Long empId = employee.getId();

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "travel_doc/" + travelPlanId +"_" + docTypeId + "_" + empId  + "_" + originalFilePath;

        file.transferTo(new File(System.getProperty("user.dir") + "/" +folderPath + filePath));

        TravelDoc travelDoc = new TravelDoc();
        travelDoc.setFkEmployee(employeeRepository.findEmployeeById(empId));
        if(travelPlanRepository.findTravelPlanById(travelPlanId) == null){
            throw new RemoteException("travel plan not found");
        }
        travelDoc.setFkTravelPlan(travelPlanRepository.findTravelPlanById(travelPlanId));
        travelDoc.setTravelDocUrl(URL + filePath);
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

                for (Long id : employeeIdByTravelPlanId) {
                    notificationService.createNotification(id
                            , "Expense Upload reminder"
                            , "For Travel Plan : "
                                    + travelPlan.getTravelPlanName() +
                                    " You have now only 5 days remaining to add expense "
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

}
