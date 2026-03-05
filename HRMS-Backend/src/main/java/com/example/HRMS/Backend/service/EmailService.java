package com.example.HRMS.Backend.service;

import java.util.List;

public interface EmailService {

    void sendEmail(List<String> email, String sub, String content);

    void sendEmailWithAttachment(List<String> to, String subject, String text, String path);
}
