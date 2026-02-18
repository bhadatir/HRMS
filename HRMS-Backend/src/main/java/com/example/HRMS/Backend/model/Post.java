package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_post_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull(message = "post title is required")
    @Column(name = "post_title", nullable = false)
    private String postTitle;

    @NotNull(message = "post content is required")
    @Column(name = "post_content", nullable = false)
    private String postContent;

    @NotNull(message = "post content url is required")
    @Column(name = "post_content_url")
    private String postContentUrl;

    @Column(name = "post_created_at")
    private Instant postCreatedAt = Instant.now();

    @Column(name = "post_is_deleted")
    private Boolean postIsDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_post_employee_id")
    private Employee fkPostEmployee;

    @ManyToOne
    @JoinColumn(name = "fk_post_visibility_id")
    private PostVisibility fkPostVisibility;
}