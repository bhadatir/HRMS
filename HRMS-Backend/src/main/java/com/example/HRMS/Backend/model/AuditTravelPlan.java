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
@Table(name = "audit_travel_plan")
public class AuditTravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_audit_travel_plan_id", nullable = false)
    private Long id;

    @NotNull(message = "action is required")
    @Column(name = "audit_travel_plan_action", nullable = false)
    private String action;

    @NotNull(message = "owner email is required")
    @Column(name = "audit_travel_plan_owner_email", nullable = false)
    private String ownerEmail;

    @ElementCollection
    @CollectionTable(
            name = "added_members",
            joinColumns = @JoinColumn(name = "member_email")
    )
    @Column(name = "audit_added_travel_members")
    private List<String> addedTravelMembers;

    @ElementCollection
    @CollectionTable(
            name = "removed_members",
            joinColumns = @JoinColumn(name = "member_email")
    )
    @Column(name = "audit_removed_travel_members")
    private List<String> removedTravelMembers;

    @Column(name = "audit_travel_plan_performed_by")
    private String performedBy;

    @Column(name = "audit_travel_plan_timestamp", nullable = false)
    private Instant timestamp = Instant.now();
}

