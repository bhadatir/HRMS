package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class NotificationResponse {
    private Long id;

    private Long employeeId;

    private String employeeEmail;

    private String title;

    private String message;

    private boolean isRead;

    private Instant createdAt;
}
