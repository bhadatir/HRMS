package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"position\"")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_position_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull(message = "position name is required")
    @Column(name = "position_name", nullable = false)
    private String positionName;


}