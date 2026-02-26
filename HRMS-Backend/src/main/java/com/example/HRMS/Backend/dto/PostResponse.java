package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.PostTag;
import com.example.HRMS.Backend.model.PostVisibility;
import com.example.HRMS.Backend.model.TagType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PostResponse {

    private Long id;

    private String postTitle;

    private String postContent;

    private String postContentUrl;

    private Instant postCreatedAt;

    private Boolean postIsDeleted;

    private Long employeeId;

    private Integer commentCount;

    private String employeeEmail;

    private String employeeFirstName;

    private Long postVisibilityId;

    private String postVisibilityName;

    private String reasonForDeletePost;

    private List<PostTagResponse> postTagResponses;

    private List<String> recentLikerNames;
}
