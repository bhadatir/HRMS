package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "booking_participants")
public class BookingParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_participant_id", nullable = false)
    private Integer id;

    @NotNull(message = "Booking id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_game_booking_id", nullable = false)
    private GameBooking fkGameBooking;

    @NotNull(message = "Employee id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_employee_id", nullable = false)
    private Employee fkEmployee;

    @NotNull(message = "GameBookingStatus id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_participant_game_booking_status_id", nullable = false)
    private GameBookingStatus fkParticipantGameBookingStatus;

}