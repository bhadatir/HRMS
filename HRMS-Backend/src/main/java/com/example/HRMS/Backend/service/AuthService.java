package com.example.HRMS.Backend.service;


import com.example.HRMS.Backend.dto.AuthRequest;
import com.example.HRMS.Backend.dto.AuthResponse;
import com.example.HRMS.Backend.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);

    void register(RegisterRequest registerRequest);
}
