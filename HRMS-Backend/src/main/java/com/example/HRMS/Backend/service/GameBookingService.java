package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.BookingParticipant;
import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.GameType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GameBookingService {

    String attemptBooking(GameBookingRequest gameBookingRequest);

    List<GameBookingResponse> findAllGameBooking();

    void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, LocalDateTime end, Boolean isFirstGame, List<Long> bookingParticipantsEmpId);

    void updateGameBookingStatus(Long pkGameBookingId, Long fkGameStatusId);

    void updateWaitingList(GameType gameType, LocalDateTime targetedSloatTime);

    Page<GameBookingResponse> findBookingByEmpId(Long empId, String searchTerm, int page, int size);

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

    List<SlotAvailabilityResponse> getAvailableSlots(Long gameTypeId, Long employeeId, LocalDate date);

    }
