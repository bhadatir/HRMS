package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.GameType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface GameTypeRepository extends JpaRepository<GameType, Long> {
    GameType findGameTypeById(long id);
}
