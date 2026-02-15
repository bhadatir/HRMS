package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_audit_log_id", nullable = false)
    private Long id;

    @NotNull(message = "entityName is required")
    @Column(name = "audit_log_entity_name", nullable = false)
    private String entityName;

    @NotNull(message = "entityId is required")
    @Column(name = "audit_log_entity_id", nullable = false)
    private Long entityId;

    @NotNull(message = "action is required")
    @Column(name = "audit_log_action", nullable = false)
    private String action;

    @Column(name = "audit_log_new_status")
    private String newStatus = "NA";

    @Column(name = "audit_log_performed_by")
    private String performedBy;

    @Column(name = "audit_log_timestamp", nullable = false)
    private Instant timestamp = Instant.now();
}

