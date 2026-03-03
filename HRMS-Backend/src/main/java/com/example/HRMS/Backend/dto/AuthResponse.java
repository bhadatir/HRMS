package com.example.HRMS.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String isFirstLogin;

    public AuthResponse(String accessToken, String isFirstLogin) {
        this.accessToken = accessToken;
        this.isFirstLogin = isFirstLogin;
        System.out.println(isFirstLogin);
    }
}
