package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.BookingWaitingListResponse;
import com.example.HRMS.Backend.dto.EmployeeGameInterestResponse;
import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.dto.GameBookingResponse;
import com.example.HRMS.Backend.model.BookingParticipant;
import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.GameType;

import java.time.LocalDateTime;
import java.util.List;

public interface GameBookingService {

    String attemptBooking(GameBookingRequest gameBookingRequest);

    List<GameBookingResponse> findAllGameBooking();

    void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, LocalDateTime end, Boolean isFirstGame, List<Long> bookingParticipantsEmpId);

    void updateGameBookingStatus(Long pkGameBookingId, Long fkGameStatusId);

    void updateWaitingList(GameType gameType, LocalDateTime targetedSloatTime);

    List<GameBookingResponse> findBookingByEmpId(Long empId);

    void updateGameBooking(Long bookingId, GameBookingRequest gameBookingRequest);

    void updateGame(Long gameTypeId, GameType gameType);

    void addGameInterest(Long empId, Long gameTypeId);

    void removeGameInterest(Long gameInterestId);

    List<EmployeeGameInterestResponse> findGameInterestByEmp(Long empId);

    List<BookingWaitingListResponse> findWaitList();

    BookingWaitingListResponse findWaitListById(Long waitId);

    List<BookingWaitingListResponse> findWaitListSeqByGameTypeAndSloat(Long waitId);

    void deleteWaitListById(Long waitId);

    List<BookingWaitingListResponse> findWaitListbyEmpId(Long empId);

    List<GameBookingResponse> findUpcommingBooking();
}
