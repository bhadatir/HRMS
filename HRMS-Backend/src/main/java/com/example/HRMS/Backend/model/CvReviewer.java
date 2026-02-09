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
@Table(name = "cv_reviewer")
public class CvReviewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_cv_reviewer_id", nullable = false)
    private Long id;

    @PastOrPresent(message = "reviewer created date cannot be in the future")
    @ColumnDefault("getdate()")
    @Column(name = "cv_reviewer_created_at")
    private Instant cvReviewerCreatedAt;

    @NotNull(message = "Reviewer id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_cv_reviewer_employee_id", nullable = false)
    private Employee fkCvReviewerEmployee;

    @NotNull(message = "job id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_job_id", nullable = false)
    private Job fkJob;


}