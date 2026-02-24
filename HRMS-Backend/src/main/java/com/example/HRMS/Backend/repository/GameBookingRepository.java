package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.GameType;
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


    @Query("SELECT COUNT(gameBooking) > 0 FROM GameBooking gameBooking WHERE gameBooking.fkHostEmployee.id = :empId " +
            "AND gameBooking.fkGameType.id = :gameTypeId " +
            "AND gameBooking.fkGameBookingStatus.id = 1 " +
            "AND gameBooking.gameBookingStartTime > (SELECT gameType.lastCycleResetDatetime FROM GameType gameType " +
            "WHERE gameType.id = :gameTypeId)")
    boolean hasActiveBookingInCycle(@Param("empId") Long empId,
                             @Param("gameTypeId") Long gameTypeId);

    boolean existsByFkGameTypeAndGameBookingStartTimeAndFkGameBookingStatus_Id(GameType gameType, LocalDateTime targetSlot, int i);

    List<GameBooking> findAllByGameBookingEndTimeBeforeAndFkGameBookingStatus_Id(LocalDateTime now, int i);

    List<GameBooking> findAllByGameBookingStartTimeBetween(LocalDateTime now, LocalDateTime targetSlot);
}
