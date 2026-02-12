package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class PostResponse {

    private Long id;

    private String postTitle;

    private String postContent;

    private String postContentUrl;

    private Instant postCreatedAt;

    private Boolean postIsDeleted;

    private Long employeeId;
}
