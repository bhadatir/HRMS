package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "post_visibility")
public class PostVisibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_post_visibility_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull(message = "post title is required")
    @Column(name = "post_visibility_name", nullable = false)
    private String postVisibilityName;

}
