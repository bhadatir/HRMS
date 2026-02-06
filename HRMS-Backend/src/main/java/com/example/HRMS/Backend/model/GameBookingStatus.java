package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_booking_status")
public class GameBookingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_game_booking_status_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull(message = "game booking status name is required")
    @Column(name = "game_booking_status_name", nullable = false, length = 50)
    private String gameBookingStatusName;


}