package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.GameBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameBookingRepository extends JpaRepository<GameBooking, Long> {

    List<GameBooking> findGameBookingByFkGameType_Id(Long fkGameTypeId);

    GameBooking findGameBookingByFkHostEmployee_Id(Long fkHostEmployeeId);

    GameBooking findGameBookingById(Long pkGameBookingId);

    @Query("SELECT COUNT(gameBooking) > 0 FROM GameBooking gameBooking WHERE gameBooking.fkHostEmployee.id = :empId " +
            "AND gameBooking.fkGameType.id = :gameTypeId " +
            "AND gameBooking.gameBookingStartTime > (SELECT gameType.lastCycleResetDatetime FROM GameType gameType " +
            "WHERE gameType.id = :gameTypeId)")
    boolean hasPlayedInCycle(@Param("empId") Long empId,
                             @Param("gameTypeId") Long gameTypeId);

    boolean existsByFkGameType_IdAndGameBookingStartTimeAndFkGameBookingStatus_Id(Long id, LocalDateTime targetSlot, int i);
}
