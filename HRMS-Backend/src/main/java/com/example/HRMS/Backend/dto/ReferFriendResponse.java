package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ReferFriendResponse {
    private Long id;

    private String referFriendName;

    private String referFriendEmail;

    private String referFriendCvUrl;

    private String referFriendShortNote;

    private Instant referFriendReviewStatusChangedAt;

    private Long cvStatusTypeId;

    private String cvStatusTypeName;

    private Long employeeId;

    private String employeeEmail;

    private Long jobId;

    private String jobTitle;

}
