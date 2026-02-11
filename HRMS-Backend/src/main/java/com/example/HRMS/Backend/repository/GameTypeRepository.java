package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Repository
public interface GameTypeRepository extends JpaRepository<GameType, Long> {
    GameType findGameTypeById(long id);
}
