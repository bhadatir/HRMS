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
@Table(name = "travel_docs")
public class TravelDoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_travel_doc_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull(message = "travel docs is required")
    @Column(name = "travel_doc_url", nullable = false)
    private String travelDocUrl;

    @ColumnDefault("getdate()")
    @Column(name = "travel_doc_uploaded_at")
    private Instant travelDocUploadedAt;

    @NotNull(message = "travel doc type id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_travel_docs_type_id", nullable = false)
    private TravelDocsType fkTravelDocsType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_travel_plan_id")
    private TravelPlan fkTravelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_employee_travel_plan_id")
    private EmployeeTravelPlan fkEmployeeTravelPlan;


}