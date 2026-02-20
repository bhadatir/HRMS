package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_comment_id", nullable = false)
    private Long id;

    @NotNull(message = "Posi id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_post_id", nullable = false)
    private Post fkPost;

    @NotNull(message = "comment id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_comment_employee_id", nullable = false)
    private Employee fkCommentEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @NotNull(message = "Comment content is required")
    @Column(name = "comment_content", nullable = false)
    private String commentContent;

    @PastOrPresent(message = "comment created date cannot be in the future")
    @Column(name = "comment_created_at")
    private Instant commentCreatedAt = Instant.now();

    @Column(name = "comment_is_deleted")
    private Boolean commentIsDeleted = false;

    @Column(name = "reason_for_delete_comment")
    private String reasonForDeleteComment;


}