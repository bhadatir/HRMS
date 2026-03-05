package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class LikeResponse {
    private Long id;

    private Long postId;

    private String postTitle;

    private Long employeeId;

    private String employeeEmail;

    private Long commentId;

    private Instant likeCreatedAt;
}
