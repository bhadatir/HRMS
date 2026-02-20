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
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_job_id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull(message = "job title is required")
    @Column(name = "job_title", nullable = false, length = 50)
    private String jobTitle;

    @NotNull(message = "job salary is required")
    @Column(name = "job_salary", nullable = false)
    private Integer jobSalary;

    @Column(name = "job_created_at")
    private Instant jobCreatedAt = Instant.now();

    @Size(max = 255)
    @NotNull(message = "job description(JD) is required")
    @Column(name = "job_description_url", nullable = false)
    private String jobDescriptionUrl;

    @Column(name = "job_is_active")
    private Boolean jobIsActive = true;

    @Column(name = "reason_for_de_active_job")
    private String reasonForDeActiveJob;

    @NotNull(message = "job type id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_job_type_id", nullable = false)
    private JobType fkJobType;

    @NotNull(message = "job owner(HR) id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_job_owner_employee_id", nullable = false)
    private Employee fkJobOwnerEmployee;


}