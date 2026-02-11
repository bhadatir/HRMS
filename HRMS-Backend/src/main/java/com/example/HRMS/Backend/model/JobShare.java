package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "job_share")
public class JobShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_job_share_id", nullable = false)
    private Long id;

    @Column(name = "job_share_created_at")
    private Instant jobShareCreatedAt = Instant.now();

    @NotNull(message = "share by whom is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_job_share_employee_id", nullable = false)
    private Employee fkJobShareEmployee;

    @NotNull(message = "which job you share? that job id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_job_id", nullable = false)
    private Job fkJob;


}