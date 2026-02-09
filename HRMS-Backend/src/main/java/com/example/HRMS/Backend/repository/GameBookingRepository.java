package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.GameBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GameBookingRepository extends JpaRepository<GameBooking, Long> {

    GameBooking findGameBookingByFkGameType_Id(Long fkGameTypeId);

    GameBooking findGameBookingById(Long pkGameBookingId);

    @Query("SELECT COUNT(gameBooking) > 0 FROM GameBooking gameBooking WHERE gameBooking.fkHostEmployee.id = :empId " +
            "AND gameBooking.fkGameType.id = :gameId " +
            "AND gameBooking.gameBookingStartTime >= :cycleStart")
    boolean hasPlayedInCycle(@Param("empId") Long empId,
                             @Param("gameId") Long gameId,
                             @Param("cycleStart") LocalDateTime cycleStart);

}
