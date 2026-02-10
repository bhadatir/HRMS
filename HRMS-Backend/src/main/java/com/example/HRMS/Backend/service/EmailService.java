package com.example.HRMS.Backend.service;

public interface EmailService {

    void sendEmail(String email, String sub, String content);

}
