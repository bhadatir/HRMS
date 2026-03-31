package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "auth_audit")
public class AuthAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_auth_audit_id", nullable = false)
    private Long id;

    @NotBlank(message = "user email is required")
    @Column(name = "auth_audit_user_email", nullable = false)
    private String userEmail;

    @NotBlank(message = "user role is required")
    @Column(name = "auth_audit_role", nullable = false)
    private String userRoleName;

    @Column(name = "auth_audit_login_timestamp")
    private Instant loginTimestamp = Instant.now();

    @Column(name = "auth_audit_logout_timestamp")
    private Instant logoutTimestamp;

    @Column(name = "auth_audit_active_min")
    private Integer activeMin;

    @Column(name = "auth_audit_expiration_time")
    private Date expirationTime;
}

