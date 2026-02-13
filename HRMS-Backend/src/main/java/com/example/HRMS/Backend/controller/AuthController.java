package com.example.HRMS.Backend.controller;


import com.example.HRMS.Backend.dto.AuthRequest;
import com.example.HRMS.Backend.dto.AuthResponse;
import com.example.HRMS.Backend.dto.RegisterRequest;
import com.example.HRMS.Backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestParam String email) {
        authService.initiateForgotPassword(email);
        return ResponseEntity.ok("Reset link sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password updated successfully.");
    }
}
