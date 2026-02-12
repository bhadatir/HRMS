package com.example.HRMS.Backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {

    @Size(max = 255)
    @NotNull(message = "post title is required")
    private String postTitle;

    @NotNull(message = "post content is required")
    private String postContent;

    private Long fkPostEmployeeId;
}
