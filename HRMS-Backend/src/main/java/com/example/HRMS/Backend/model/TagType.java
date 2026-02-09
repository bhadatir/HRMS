package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tag_types")
public class TagType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_tag_type_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull(message = "tag type name is required")
    @Column(name = "tag_type_name", nullable = false)
    private String tagTypeName;


}