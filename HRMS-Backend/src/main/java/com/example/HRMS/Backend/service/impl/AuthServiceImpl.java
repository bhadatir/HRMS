package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.AuthService;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.TravelPlanService;
import com.example.HRMS.Backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final EmployeeRepository employeeRepository;

    private final GameTypeRepository gameTypeRepository;

    private final ModelMapper modelMapper;

    private final TravelPlanRepository travelPlanRepository;

    private final RoleRepository roleRepository;

    private final DepartmentRepository departmentRepository;

    private final PositionRepository positionRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final EmailService emailService;


    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(authentication.getName()).orElseThrow(
                () -> new RuntimeException("employee email not valid")
        );

        String isFirstLogin = "no";
        if(employee.getLastLoginAt() == null){
            isFirstLogin="yes";
        }

        employee.setLastLoginAt(Instant.now());
        employeeRepository.save(employee);

        return new AuthResponse(jwt, isFirstLogin);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        if (employeeRepository.existsByEmployeeEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Employee employee = new Employee();
        employee.setEmployeeFirstName(request.getFirstName());
        employee.setEmployeeLastName(request.getLastName());
        employee.setEmployeeEmail(request.getEmail());
        employee.setEmployeePassword(passwordEncoder.encode(request.getPassword()));
        employee.setEmployeeDob(request.getDob());
        employee.setEmployeeGender(request.getGender());
        employee.setEmployeeHireDate(request.getHireDate());
        employee.setEmployeeSalary(request.getSalary());

        Department department = departmentRepository.findById(Long.valueOf(request.getDepartmentId()))
                .orElseThrow(() -> new RuntimeException("Department not found"));
        employee.setFkDepartment(department);

        Position position = positionRepository.findById(Long.valueOf(request.getPositionId()))
                .orElseThrow(() -> new RuntimeException("Position not found"));
        employee.setFkPosition(position);

        Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
                .orElseThrow(() -> new RuntimeException("Role not found"));
        employee.setFkRole(role);

        employeeRepository.save(employee);

        List<String> emails1 = new ArrayList<>();
        emails1.add(request.getEmail());
        emailService.sendEmail(emails1,"Congratulation you are added in ROIMA as : " + role.getRoleName()
                ,"Please update your password using given detail in ROIMA HRMS : email - "
                + request.getEmail() + " pass - " + request.getPassword()
        );


    }

    @Override
    public void initiateForgotPassword(String email) {
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        employee.setResetToken(token);
        employee.setResetTokenExpiry(LocalDateTime.now().plusMinutes(1));
        employeeRepository.save(employee);

        List<String> emails = new ArrayList<>();
        emails.add(email);

        emailService.sendEmail(emails, "Password Reset", "Your token is: " + token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Employee employee = employeeRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (employee.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        employee.setEmployeePassword(passwordEncoder.encode(newPassword));
        employee.setResetToken(null);
        employeeRepository.save(employee);
    }

    @Override
    public void updatePassword(Long empId, String newPassword){
        Employee employee = employeeRepository.findEmployeeById(empId);
        employee.setEmployeePassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);

        List<String> emails1 = new ArrayList<>();
        emails1.add(employee.getEmployeeEmail());
        emailService.sendEmail(emails1,"your password updated at : " + Instant.now()
                ,"Please verify your password updation and if any problem so contact us immediately."
        );
    }

    @Override
    public EmployeeResponse getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow( () -> new RuntimeException("employee not found"));
        return modelMapper.map(employee,EmployeeResponse.class);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees(){
        List<Employee> employee = employeeRepository.findAll();
        return employee.stream().map(employee1 -> {
            return modelMapper.map(employee1,EmployeeResponse.class);
        }).toList();
    }

    @Value("${img.path}")
    private String folderPath;

    @Value("${URL.path}")
    private String URL;

    @Override
    public void addProfileImage(Long empId, MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findEmployeeById(empId);

        String originalFilePath = Objects.requireNonNull(file.getOriginalFilename()).replace(" ","_");
        String filePath = "Profile_Image/" + empId + originalFilePath;
        file.transferTo(new File(System.getProperty("user.dir") + "/" + folderPath + filePath));

        employee.setEmployeeProfileUrl(URL + filePath);

        employeeRepository.save(employee);
    }

    @Override
    public Page<EmployeeSearch> getEmployeeByName(String query, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.searchEmployeeByName(query, pageable);
    }

    @Override
    public Page<EmployeeSearch> getAvailableEmployeeForTravel(String query, int page, int size, LocalDate startDate, LocalDate endDate){
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeSearch> employeeSearches = employeeRepository.searchEmployeeByName(query, pageable);
        if (employeeSearches == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Page and date must not be null");
        }

        List<EmployeeSearch> filteredList = employeeSearches.getContent()
                .stream()
                .filter(emp -> {
                    return !travelPlanRepository.findAllByTravelStartTimeBetween(emp.getId(), startDate, endDate);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(filteredList, pageable, filteredList.size());
    }

    @Override
    public
    Page<EmployeeSearch> getAvailableParticipants(String query, int page, int size,
                                                  LocalDateTime startDate1,
                                                  Long gameTypeId){
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime endDate1 = startDate1.plusMinutes(gameTypeRepository.findGameTypeById(gameTypeId).getGameSlotDuration());
        return employeeRepository.searchAvailableParticipants(query, startDate1, endDate1, gameTypeId, pageable);
    }

    @Override
    public Employee getLoginUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return employeeRepository.findEmployeeByEmployeeEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    @Override
    public void updateUser(@Valid RegisterRequest request, String userEmail){
        Employee employee = employeeRepository.findEmployeeByEmployeeEmail(userEmail).orElseThrow(
                () -> new RuntimeException("employ email not found.")
        );
        if(Boolean.FALSE.equals(employee.getEmployeeIsActive())){
            throw new RuntimeException("can not edit inactive user.");
        }
        employee.setEmployeeFirstName(request.getFirstName());
        employee.setEmployeeLastName(request.getLastName());
        employee.setEmployeeEmail(request.getEmail());
        employee.setEmployeeDob(request.getDob());
        employee.setEmployeeGender(request.getGender());
        employee.setEmployeeHireDate(request.getHireDate());
        employee.setEmployeeSalary(request.getSalary());

        Department department = departmentRepository.findById(Long.valueOf(request.getDepartmentId()))
                .orElseThrow(() -> new RuntimeException("Department not found"));
        employee.setFkDepartment(department);

        Position position = positionRepository.findById(Long.valueOf(request.getPositionId()))
                .orElseThrow(() -> new RuntimeException("Position not found"));
        employee.setFkPosition(position);

        Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
                .orElseThrow(() -> new RuntimeException("Role not found"));
        employee.setFkRole(role);

        employeeRepository.save(employee);

        List<String> emails1 = new ArrayList<>();
        emails1.add(request.getEmail());
        emailService.sendEmail(emails1,"your details is updated at : " + Instant.now()
                ,"Please verify your details and if any problem so contact us immediately."
        );
    }

    @Override
    public List<Position> showAllPosition(){
        return positionRepository.findAll();
    }

    @Override
    public List<Department> showAllDepartments(){
        return departmentRepository.findAll();
    }

    @Override
    public List<Role> showAllRoles(){
        return roleRepository.findAll();
    }

    @Override
    public void inActiveUserById(Long userId, String reason){
        Employee employee = employeeRepository.findEmployeeById(userId);
        if(Boolean.FALSE.equals(employee.getEmployeeIsActive())){
            throw new RuntimeException("can not make inactive who is already inactive user.");
        }
        employee.setEmployeeIsActive(false);
        employee.setReasonForInActive(reason);
        employeeRepository.save(employee);

        List<String> emails1 = new ArrayList<>();
        emails1.add(employee.getEmployeeEmail());
        emailService.sendEmail(emails1,"your Status is inactivated at : " + Instant.now()
                ,"Reason for Inactivation : " + reason
        );
    }
}
