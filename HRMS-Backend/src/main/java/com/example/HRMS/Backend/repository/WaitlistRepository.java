package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.BookingWaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WaitlistRepository extends JpaRepository<BookingWaitingList,Long> {
    BookingWaitingList findBookingWaitingListsById(Long waitingListId);

    @Query("SELECT COUNT(bookingWaitingList) > 0 FROM BookingWaitingList bookingWaitingList " +
            "WHERE bookingWaitingList.fkHostEmployee.id = :empId " +
            "AND bookingWaitingList.fkGameType.id = :gameId " +
            "AND bookingWaitingList.targetSlotDatetime > (SELECT gameType.lastCycleResetDatetime FROM GameType gameType " +
            "WHERE gameType.id = :gameTypeId)")
    boolean hasAppliedInCycle(@Param("empId") Long empId,
                             @Param("gameId") Long gameId);
}
