package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.dto.BookingParticipantResponse;
import com.example.HRMS.Backend.model.BookingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingParticipantRepository extends JpaRepository<BookingParticipant,Long> {

    @Query(value = "Select new com.example.HRMS.Backend.dto.BookingParticipantResponse( " +
            "b.id, " +
            "b.fkBookingWaitingList.id, " +
            "b.fkEmployee.employeeEmail, " +
            "b.fkEmployee.employeeFirstName, b.fkEmployee.employeeLastName) from " +
            "BookingParticipant b where b.fkGameBooking.id = :id ")
    List<BookingParticipantResponse> findAllByGameBookingId(Long id);

    @Query(value = "Select new com.example.HRMS.Backend.dto.BookingParticipantResponse( " +
            "b.id, " +
            "b.fkBookingWaitingList.id, " +
            "b.fkEmployee.employeeEmail, " +
            "b.fkEmployee.employeeFirstName, b.fkEmployee.employeeLastName) from " +
            "BookingParticipant b where b.fkBookingWaitingList.id = :bookingWaitingListId ")
    List<BookingParticipantResponse> findAllByBookingWaitingListId(Long bookingWaitingListId);

    List<BookingParticipant> findByFkBookingWaitingList_Id(Long id);
}
