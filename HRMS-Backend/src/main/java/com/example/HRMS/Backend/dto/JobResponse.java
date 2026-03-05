package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class JobResponse {

    private Long id;

    private String jobTitle;

    private Integer jobSalary;

    private Instant jobCreatedAt;

    private String jobDescriptionUrl;

    private Boolean jobIsActive;

    private Long jobTypeId;

    private String jobTypeName;

    private String reasonForDeActiveJob;

    private Long employeeId;
    private String employeeEmail;

    private List<CvReviewerResponse> cvReviewerResponses;

}
