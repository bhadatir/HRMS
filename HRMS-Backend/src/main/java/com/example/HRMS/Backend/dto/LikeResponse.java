package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Comment;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Post;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
