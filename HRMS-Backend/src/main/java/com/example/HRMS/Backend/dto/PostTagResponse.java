package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Post;
import com.example.HRMS.Backend.model.TagType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostTagResponse {

    private Long id;

    private Long postId;

    private String postTitle;

    private Long tagTypeId;

    private String tagTypeName;

}
