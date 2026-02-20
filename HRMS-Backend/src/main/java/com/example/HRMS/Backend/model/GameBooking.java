package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "game_booking")
public class GameBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_game_booking_id", nullable = false)
    private Long id;

    @Column(name = "game_booking_is_deleted")
    private Boolean gameBookingIsDeleted = false;

    @NotNull(message = "game booking start time is required")
    @Column(name = "game_booking_start_time", nullable = false)
    private LocalDateTime gameBookingStartTime;

    @NotNull(message = "game booking end is required")
    @Column(name = "game_booking_end_time", nullable = false)
    private LocalDateTime gameBookingEndTime;

    @NotNull(message = "game booking status id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_game_booking_status_id", nullable = false)
    private GameBookingStatus fkGameBookingStatus;

    @NotNull(message = "game type id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_game_type_id", nullable = false)
    private GameType fkGameType;

    @NotNull(message = "host employee id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_host_employee_id", nullable = false)
    private Employee fkHostEmployee;

    @Column(name = "game_booking_created_at")
    private Instant gameBookingCreatedAt = Instant.now();


}