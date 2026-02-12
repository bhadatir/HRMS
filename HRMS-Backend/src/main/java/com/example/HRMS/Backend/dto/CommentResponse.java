package com.example.HRMS.Backend.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class CommentResponse {

    private Long id;

    private Long postId;

    private Long employeeId;

    private Long parentCommentId;

    private String commentContent;

    private Instant commentCreatedAt;

    private Boolean commentIsDeleted;

}
