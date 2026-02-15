package com.example.HRMS.Backend.service;


import com.example.HRMS.Backend.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);

    void register(RegisterRequest registerRequest);

    void resetPassword(String token, String newPassword);

    void initiateForgotPassword(String email);

    EmployeeResponse getEmployeeByEmail(String email) throws IOException;

    void addProfileImage(Long empId, MultipartFile file) throws IOException;

    List<EmployeeSearch> getEmployeeByName(String query);
}
