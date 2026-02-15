package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
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
