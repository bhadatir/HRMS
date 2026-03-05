package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.Department;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Position;
import com.example.HRMS.Backend.model.Role;
import jakarta.validation.Valid;
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

    EmployeeResponse getEmployeeByEmail(String email);

    void addProfileImage(Long empId, MultipartFile file) throws IOException;

    Page<EmployeeSearch> getEmployeeByName(String query, int page, int size);

    Employee getLoginUser();

    Page<EmployeeSearch> getAvailableEmployeeForTravel(String query, int page, int size, LocalDate startDate, LocalDate endDate);

    Page<EmployeeSearch> getAvailableParticipants(String query, int page, int size, LocalDateTime startDate1, Long gameTypeId);

    List<EmployeeResponse> getAllEmployees(String searchTerm);

    void updateUser(@Valid RegisterRequest registerRequest, String userEmail);

    List<Position> showAllPosition();

    List<Department> showAllDepartments();

    List<Role> showAllRoles();

    void inActiveUserById(Long userId, String reason);

    void updatePassword(Long empId, String newPassword);

    void logout();
}
