package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
@Entity
@Table(name = "game_type")
public class GameType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_game_type_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull(message = "game name is required")
    @Column(name = "game_name", nullable = false, length = 50)
    private String gameName;

    @NotNull(message = "operating start time is required")
    @Column(name = "operating_start", nullable = false)
    private Time operatingStart;

    @NotNull(message = "operating end time is required")
    @Column(name = "operating_end", nullable = false)
    private Time operatingEnd;

    @NotNull(message = "game slot duration is required")
    @Column(name = "game_slot_duration", nullable = false)
    private Integer gameSlotDuration;

    @NotNull(message = "max player per game is required")
    @Column(name = "game_max_player_per_slot", nullable = false)
    private Integer gameMaxPlayerPerSlot;


}