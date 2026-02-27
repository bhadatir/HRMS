package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.dto.BookingParticipantResponse;
import com.example.HRMS.Backend.model.BookingParticipant;
import com.example.HRMS.Backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingParticipantRepository extends JpaRepository<BookingParticipant,Long> {

    @Query(value = "Select new com.example.HRMS.Backend.dto.BookingParticipantResponse( " +
            "b.id, " +
            "b.fkBookingWaitingList.id, " +
            "b.fkEmployee.id,  " +
            "b.fkEmployee.employeeEmail, " +
            "b.fkEmployee.employeeFirstName, b.fkEmployee.employeeLastName) from " +
            "BookingParticipant b where b.fkGameBooking.id = :id ")
    List<BookingParticipantResponse> findAllByGameBookingId(Long id);

    @Query(value = "Select new com.example.HRMS.Backend.dto.BookingParticipantResponse( " +
            "b.id, " +
            "b.fkBookingWaitingList.id, " +
            "b.fkEmployee.id, " +
            "b.fkEmployee.employeeEmail, " +
            "b.fkEmployee.employeeFirstName, b.fkEmployee.employeeLastName) from " +
            "BookingParticipant b where b.fkBookingWaitingList.id = :bookingWaitingListId ")
    List<BookingParticipantResponse> findAllByBookingWaitingListId(Long bookingWaitingListId);

    List<BookingParticipant> findByFkBookingWaitingList_Id(Long id);

    @Query(value = "select p.fkEmployee.id " +
            "from BookingParticipant p " +
            "where p.fkGameBooking.id = :bookingId ")
    List<Long> findEmployeeIdByGameBookingId(Long bookingId);

    @Query(value = "select BookingParticipant " +
            "from BookingParticipant p " +
            "where p.fkGameBooking.id = :bookingId " +
            "And p.fkEmployee.id = :id ")
    BookingParticipant findBookingParticipantByEmployeeIdAndGameBookingId(Long id, Long bookingId);

    void removeBookingParticipantById(Long id);

    @Query("SELECT COUNT(b) > 0 FROM BookingParticipant b " +
            "WHERE b.fkEmployee.id = :empId " +
            "AND ((:start < b.fkGameBooking.gameBookingEndTime AND :end > b.fkGameBooking.gameBookingStartTime))")
    boolean existsOverlappingBookingParticipant(@Param("empId") Long empId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(b) > 0 FROM BookingParticipant b " +
            "WHERE b.fkEmployee.id = :empId " +
            "AND ((:start < b.fkBookingWaitingList.targetSlotEndDatetime AND :end > b.fkBookingWaitingList.targetSlotDatetime))")
    boolean existsOverlappingBookingWaitingListParticipant(@Param("empId") Long empId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    List<BookingParticipant> findBookingParticipantByFkEmployee(Employee emp);
}
