package com.example.HRMS.Backend.service;


import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);

    void register(RegisterRequest registerRequest);

    void resetPassword(String token, String newPassword);

    void initiateForgotPassword(String email);

    EmployeeResponse getEmployeeByEmail(String email) throws IOException;

    void addProfileImage(Long empId, MultipartFile file) throws IOException;

    Page<EmployeeSearch> getEmployeeByName(String query, int page, int size);

    Employee getLoginUser();

    Page<EmployeeSearch> getAvailableEmployeeForTravel(String query, int page, int size, LocalDate startDate, LocalDate endDate);
}
