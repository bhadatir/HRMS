package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "booking_waiting_list")
public class BookingWaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_waiting_id", nullable = false)
    private Long id;

    @ColumnDefault("getdate()")
    @Column(name = "target_slot_datetime")
    private LocalDateTime targetSlotDatetime;

    @Column(name = "is_first_game")
    private Boolean isFirstGame = true;

    @Column(name = "waiting_status_is_active", nullable = false)
    private Boolean waitingStatusIsActive = true;

    @NotNull(message = "List Creation date and time is required")
    @Column(name = "waiting_list_created_at", nullable = false)
    private LocalDateTime waitingListCreatedAt = LocalDateTime.now();

    @NotNull(message = "game type id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_game_type_id", nullable = false)
    private GameType fkGameType;

    @NotNull(message = "host employee id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_host_employee_id", nullable = false)
    private Employee fkHostEmployee;

}

