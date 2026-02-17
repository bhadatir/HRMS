package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.EmployeeTravelPlanResponse;
import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.dto.TravelPlanResponse;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.TravelPlanService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.*;

import static org.hibernate.type.descriptor.java.CoercionHelper.toLong;

@Service
@RequiredArgsConstructor
public class TravelPlanServiceImpl implements TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;

    private final ModelMapper modelMapper;

    private final TravelDocRepository travelDocRepository;

    private final TravelPlanStatusRepository travelPlanStatusRepository;

    private final EmployeeTravelPlanRepository employeeTravelPlanRepository;

    private final EmployeeRepository employeeRepository;

    private final TravelDocsTypeRepository travelDocsTypeRepository;

    private final EmailService emailService;

    @Override
    public TravelPlan findTravelPlanByHREmployeeId(Long hrEmployeeId){
        return travelPlanRepository.findTravelPlanByFkTravelPlanHREmployee_Id(hrEmployeeId);
    }

    @Transactional
    @Override
    public void addTravelPlan(TravelPlanRequest travelPlanRequest){

        TravelPlan travelPlan = modelMapper.map(travelPlanRequest, TravelPlan.class);

        travelPlan.setId(null);

        TravelPlan savedTravelplan = travelPlanRepository.save(travelPlan);

        TravelPlanStatus travelPlanStatus = travelPlanStatusRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("Status not found")
        );

        List<Long> empId = travelPlanRequest.getEmployeesInTravelPlanId();

        List<String> emails = new ArrayList<>();

        for(Long id : empId){
            Employee employee = employeeRepository.findById(id).orElseThrow(
                    () -> new RuntimeException("Employee not found"));

            EmployeeTravelPlan employeeTravelPlan = new EmployeeTravelPlan();
            employeeTravelPlan.setFkEmployee(employee);
            employeeTravelPlan.setEmployeeTravelPlanCreatedAt(Instant.now());
            employeeTravelPlan.setFkTravelPlan(savedTravelplan);
            employeeTravelPlan.setFkTravelPlanStatus(travelPlanStatus);
            employeeTravelPlanRepository.save(employeeTravelPlan);

            emails.add(employee.getEmployeeEmail());
        }

        emailService.sendEmail(emails,"Travel Plan",travelPlanRequest.getTravelPlanDetails());
    }

    @Override
    public void updateTravelPlan(@Valid TravelPlanRequest travelPlanRequest, Long travelPlanId){

        TravelPlan travelPlan = modelMapper.map(travelPlanRequest, TravelPlan.class);

        travelPlan.setId(travelPlanId);

        TravelPlan savedTravelplan = travelPlanRepository.save(travelPlan);

        TravelPlanStatus travelPlanStatus = travelPlanStatusRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("Status not found")
        );

        List<Long> empId = travelPlanRequest.getEmployeesInTravelPlanId();

        List<String> emails = new ArrayList<>();

        for(Long id : empId){

            Long empTravelId = employeeTravelPlanRepository.findEmployeeTravelPlanByEmployeeIdAndTravelPlanId(id,travelPlanId);

            if(empTravelId == null) {
                Employee employee = employeeRepository.findById(id).orElseThrow(
                        () -> new RuntimeException("Employee not found"));

                EmployeeTravelPlan employeeTravelPlan = new EmployeeTravelPlan();
                employeeTravelPlan.setFkEmployee(employee);
                employeeTravelPlan.setEmployeeTravelPlanCreatedAt(Instant.now());
                employeeTravelPlan.setFkTravelPlan(savedTravelplan);
                employeeTravelPlan.setFkTravelPlanStatus(travelPlanStatus);
                employeeTravelPlanRepository.save(employeeTravelPlan);

                emails.add(employee.getEmployeeEmail());
            }
        }

        if(!emails.isEmpty()){
        emailService.sendEmail(emails,"Travel Plan",travelPlanRequest.getTravelPlanDetails());}
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
        for(TravelPlan travelPlan:travelPlanRepository.findAll()){
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

    @Value("${img.path}")
    private String folderPath;

    @Value("${URL.path}")
    private String URL;

    @Override
    public void saveDocByEmployee(Long employeeTravelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException {

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

        EmployeeTravelPlan employeeTravelPlan = employeeTravelPlanRepository.findEmployeeTravelPlanById(employeeTravelPlanId);

        if(employeeTravelPlan == null){
            throw new RemoteException("employeeTravelPlan not found");
        }

        travelDoc.setFkEmployeeTravelPlan(employeeTravelPlan);
        travelDoc.setTravelDocUrl(URL + filePath);
        travelDoc.setTravelDocUploadedAt(Instant.now());
        travelDoc.setFkTravelDocsType(travelDocsTypeRepository.findTravelDocsTypeById(docTypeId));

        travelDocRepository.save(travelDoc);
    }

    @Override
    public void saveDocByHr(Long travelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException {

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
    public List<TravelDocResponse> findAllTravelPlanDocByTravelPlan(Long travelPlanId) {
        TravelPlan travelPlan = travelPlanRepository.findTravelPlanById(travelPlanId);
        List<TravelDoc> travelDocsHr = travelDocRepository.findByFkTravelPlan(travelPlan);

        System.out.println(travelDocsHr);

        List<EmployeeTravelPlan> employeeTravelPlan = employeeTravelPlanRepository.
                findEmployeeTravelPlanByFkTravelPlan_Id(travelPlanId);

        Set<TravelDoc> travelDocsSet = new LinkedHashSet<>(travelDocsHr);

        for (EmployeeTravelPlan etp : employeeTravelPlan)
        {
            List<TravelDoc> travelDocsEmp = travelDocRepository.
                    findTravelDocsByFkEmployeeTravelPlan_Id(etp.getId());
            System.out.println(travelDocsEmp);
            travelDocsSet.addAll(travelDocsEmp);
        }

        List<TravelDoc> travelDocs = new ArrayList<>(travelDocsSet);
        List<TravelDocResponse> travelDocResponses = new ArrayList<>();
        for(TravelDoc travelDoc : travelDocs)
        {
            TravelDocResponse travelDocResponse = modelMapper.map(travelDoc,TravelDocResponse.class);
            travelDocResponses.add(travelDocResponse);
        }

        return travelDocResponses;
    }


//    @Cacheable(value = "TravelDoc", key = "#id")
//    public byte[] getTravelDocImg(Long id, String path) throws IOException {
//        return Files.readAllBytes(new File(System.getProperty("user.dir") + "/" + path).toPath());
//    }

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


}
