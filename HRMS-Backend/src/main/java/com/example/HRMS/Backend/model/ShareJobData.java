
package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
        import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Share_job_data")
public class ShareJobData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_Share_job_data_id", nullable = false)
    private Long id;

    @NotNull(message = "Who share job that is required")
    @Column(name = "share_job_by", nullable = false)
    private String shareJobBy;

    @NotNull(message = "receiver Email is required")
    @Column(name = "receiver_email", nullable = false)
    private String receiverEmail;

    @NotNull(message = "Job id is required")
    @Column(name = "fk_job_id", nullable = false)
    private Long fkJobId;

    @Column(name = "job_share_at", nullable = false)
    private Instant timestamp = Instant.now();
}

