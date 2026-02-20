package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<BookingWaitingList,Long> {

    BookingWaitingList findBookingWaitingListsById(Long waitingListId);

    @Query("SELECT COUNT(bookingWaitingList) > 0 FROM BookingWaitingList bookingWaitingList " +
            "WHERE bookingWaitingList.fkHostEmployee.id = :empId " +
            "AND bookingWaitingList.fkGameType.id = :gameTypeId " +
            "AND bookingWaitingList.targetSlotDatetime > (SELECT gameType.lastCycleResetDatetime FROM GameType gameType " +
            "WHERE gameType.id = :gameTypeId)")
    boolean hasAppliedInCycle(@Param("empId") Long empId,
                             @Param("gameTypeId") Long gameTypeId);

    @Query(value = "SELECT bwl.pk_waiting_id, bwl.is_first_game, bwl.target_slot_datetime, bwl.waiting_list_created_at " +
            ", bwl.waiting_status_is_active, bwl.fk_game_type_id, bwl.fk_host_employee_id " +
            "FROM booking_waiting_list bwl inner join employee_game_interest egi on egi.fk_employee_id = bwl.fk_host_employee_id " +
            "WHERE bwl.fk_game_type_id = :gameTypeId " +
            "AND bwl.target_slot_datetime = :targetSlotDatetime " +
            "order by egi.played_in_current_cycle ASC, " +
            "bwl.waiting_list_created_at ASC",
            nativeQuery = true)
    List<BookingWaitingList> findMatchingBookings(
            @Param("gameTypeId") Long gameTypeId,
            @Param("targetSlotDatetime") LocalDateTime targetSlotDatetime);

    void removeBookingWaitingListsById(Long id);
}
