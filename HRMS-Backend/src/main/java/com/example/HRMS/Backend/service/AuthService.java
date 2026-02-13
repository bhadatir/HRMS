package com.example.HRMS.Backend.service;


import com.example.HRMS.Backend.dto.AuthRequest;
import com.example.HRMS.Backend.dto.AuthResponse;
import com.example.HRMS.Backend.dto.EmployeeResponse;
import com.example.HRMS.Backend.dto.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);

    void register(RegisterRequest registerRequest);

    void resetPassword(String token, String newPassword);

    void initiateForgotPassword(String email);

    EmployeeResponse getEmployeeByEmail(String email) throws IOException;

    void addProfileImage(Long empId, MultipartFile file) throws IOException;
}
