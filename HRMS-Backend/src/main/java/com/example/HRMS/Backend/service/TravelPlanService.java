package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.TravelDocResponse;
import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.dto.TravelPlanResponse;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.model.TravelPlan;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TravelPlanService {
    TravelPlan findTravelPlanByHREmployeeId(Long hrEmployeeId);

    void addTravelPlan(TravelPlanRequest travelPlanRequest);

    List<TravelPlanResponse> showAllTravelPlan();

    void saveDoc(Long travelPlanId, MultipartFile file, Long docTypeId) throws IOException;

    List<TravelDocResponse> findAllTravelDoc() throws IOException;

    void updateTravelPlan(@Valid TravelPlan travelPlan);

    Long findEmployeeTravelPlanId(Long empId, Long travelId);
}
