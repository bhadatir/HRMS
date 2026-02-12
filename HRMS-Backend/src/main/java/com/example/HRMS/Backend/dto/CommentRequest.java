package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.Comment;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Post;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Data
public class CommentRequest {

    @NotNull(message = "Post id is required")
    private Long fkPostId;

    @NotNull(message = "comment id is required")
    private Long fkCommentEmployeeId;

    private Long parentCommentId;

    @NotNull(message = "Comment content is required")
    private String commentContent;

}
