package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "job_share_to")
public class JobShareTo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_job_share_to_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull(message = "job detail receiver email address is required")
    @Column(name = "job_share_to_email", nullable = false)
    private String jobShareToEmail;

    @NotNull(message = "which job you share? it's id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_job_share_id", nullable = false)
    private JobShare fkJobShare;


}