package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CvReviewerResponse {
    private Long id;

    private Instant cvReviewerCreatedAt;

    private Long employeeId;

    private String employeeEmail;

    private Long jobId;
}
