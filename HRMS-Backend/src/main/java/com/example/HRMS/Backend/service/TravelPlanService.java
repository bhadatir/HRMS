package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.dto.TravelPlanResponse;
import com.example.HRMS.Backend.model.TravelDocsType;
import com.example.HRMS.Backend.model.TravelPlan;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface TravelPlanService {
    TravelPlan findTravelPlanByHREmployeeId(Long hrEmployeeId);

    void addTravelPlan(TravelPlanRequest travelPlanRequest);

    List<TravelPlanResponse> showAllTravelPlan();

    void saveDocByEmployee(Long employeeTravelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException;

    void saveDocByHr(Long travelPlanId, MultipartFile file, Long docTypeId, Long employeeId) throws IOException;

    List<TravelDocResponse> findAllTravelDoc() throws IOException;

    void updateTravelPlan(@Valid TravelPlanRequest travelPlan, Long travelPlanId);

    Long findEmployeeTravelPlanId(Long empId, Long travelId);

    List<Long> getTravelPlan(String query);

    TravelPlanResponse showTravelPlanById(Long id);

    List<TravelDocResponse> findTravelDocByFkEmployeeId(Long empId,Long travelPlanId);

    List<TravelDocResponse> findAllTravelPlanDocByTravelPlan(Long travelPlanId);

    void markAsDeleted(Long travelPlanId, String reason);

    void markEmployeeTravelPlanAsDelete(Long empId, Long travelPlanId);

    List<TravelDocsType> getAllDocTypes();

    boolean isEmpAvailable(Long id, LocalDate startDate, LocalDate endDate);
}
