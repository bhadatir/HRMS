package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.dto.GameBookingResponse;
import com.example.HRMS.Backend.model.BookingParticipant;
import com.example.HRMS.Backend.model.GameBooking;

import java.time.LocalDateTime;
import java.util.List;

public interface GameBookingService {

    String attemptBooking(GameBookingRequest gameBookingRequest);

    List<GameBookingResponse> findAllGameBooking();

    void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, boolean isSecond, List<BookingParticipant> bookingParticipants);

    GameBooking updateGameBookingStatus(Long pkGameBookingId, Long fkGameStatusId);

    GameBookingResponse findBookingByEmpId(Long empId);
}
