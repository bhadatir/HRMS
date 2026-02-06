package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "travel_docs_type")
public class TravelDocsType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_travel_docs_type_id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull(message = "travel doc type name is required")
    @Column(name = "travel_docs_type_name", nullable = false, length = 20)
    private String travelDocsTypeName;


}