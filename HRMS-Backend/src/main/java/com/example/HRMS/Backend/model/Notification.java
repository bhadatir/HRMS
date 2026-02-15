package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_notification_id", nullable = false)
    private Long id;

    @NotNull(message = "Employee id is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_employee_id")
    private Employee fkEmployee;

    @NotNull(message = "notification title is required")
    @Column(name = "notification_title", nullable = false)
    private String title;

    @NotNull(message = "notification message is required")
    @Column(name = "notification_message", nullable = false)
    private String message;

    @Column(name = "notification_is_read")
    private boolean isRead = false;

    @Column(name = "notification_created_at")
    private Instant createdAt = Instant.now();

}
