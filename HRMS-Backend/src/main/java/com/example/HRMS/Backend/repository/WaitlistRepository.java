package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.GameBooking;
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

//    @Query(value = "SELECT bwl.pk_waiting_id, bwl.is_first_game, bwl.target_slot_datetime, bwl.waiting_list_created_at " +
//            ", bwl.waiting_status_is_active, bwl.fk_game_type_id, bwl.fk_host_employee_id " +
//            "FROM booking_waiting_list bwl inner join employee_game_interest egi on egi.fk_employee_id = bwl.fk_host_employee_id " +
//            "WHERE bwl.fk_game_type_id = :gameTypeId " +
//            "AND bwl.target_slot_datetime = :targetSlotDatetime " +
//            "order by egi.played_in_current_cycle ASC, " +
//            "bwl.waiting_list_created_at ASC",
//            nativeQuery = true)

    @Query(value = "WITH GroupStats AS ( " +
            "SELECT " +
            "bwl.pk_waiting_id, " +
            "AVG(egi_all.played_in_current_cycle) AS avg_group_played " +
            "FROM booking_waiting_list bwl " +
            "INNER JOIN booking_participants bp " +
            "ON bwl.pk_waiting_id = bp.fk_booking_waiting_list_id " +
            "INNER JOIN employee_game_interest egi_all " +
            "ON egi_all.fk_employee_id = bwl.fk_host_employee_id " +
            "OR egi_all.fk_employee_id = bp.fk_employee_id " +
            "WHERE bwl.fk_game_type_id = :gameTypeId " +
            "AND egi_all.fk_game_type_id = :gameTypeId " +
            "AND bwl.target_slot_datetime = :targetSlotDatetime " +
            "GROUP BY bwl.pk_waiting_id) " +
            "SELECT bwl.* " +
            "FROM GroupStats gs " +
            "JOIN booking_waiting_list bwl ON bwl.pk_waiting_id = gs.pk_waiting_id " +
            "ORDER BY gs.avg_group_played ASC, bwl.waiting_list_created_at ASC ",
            nativeQuery = true)
    List<BookingWaitingList> findMatchingBookings(
            @Param("gameTypeId") Long gameTypeId,
            @Param("targetSlotDatetime") LocalDateTime targetSlotDatetime);


    void removeBookingWaitingListById(Long id);

    List<BookingWaitingList> findAllByTargetSlotDatetimeBetween(LocalDateTime now, LocalDateTime targetSlot);

    List<BookingWaitingList> findAllByTargetSlotDatetimeBefore(LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM BookingWaitingList b " +
            "WHERE b.fkHostEmployee.id = :empId " +
            "AND b.waitingStatusIsActive = true " +
            "AND ((:start < (b.targetSlotEndDatetime) AND :end > b.targetSlotDatetime))")
    boolean existsOverlappingBookingWaitingList(@Param("empId") Long empId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    List<BookingWaitingList> findBookingWaitingListsByFkHostEmployee(Employee employeeById);
}
