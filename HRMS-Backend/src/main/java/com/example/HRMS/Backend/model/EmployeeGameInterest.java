package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employee_game_interest")
public class EmployeeGameInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_employee_game_interest_id", nullable = false)
    private Long id;

    @NotNull(message = "Employee(player) id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_employee_id", nullable = false)
    private Employee fkEmployee;

    @NotNull(message = "Game name is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_game_type_id", nullable = false)
    private GameType fkGameType;

//    @Column(name = "played_in_current_cycle")
//    private Boolean playedInCurrentCycle = false;

}
