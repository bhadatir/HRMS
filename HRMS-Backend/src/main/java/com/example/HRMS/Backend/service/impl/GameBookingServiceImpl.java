package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameBookingServiceImpl implements GameBookingService {

    private final GameBookingRepository gameBookingRepository;

    private final WaitlistRepository waitlistRepo;

    private final EmployeeRepository employeeRepository;

    private final GameTypeRepository gameTypeRepository;

    private final GameBookingStatusRepository gameBookingStatusRepository;

    private final BookingParticipantRepository bookingParticipantRepository;

    private final EmployeeGameInterestRepository employeeGameInterestRepository;

    @Autowired
    public GameBookingServiceImpl(GameBookingRepository gameBookingRepository,
                                  WaitlistRepository waitlistRepo, EmployeeRepository employeeRepository,
                                  GameTypeRepository gameTypeRepository, GameBookingStatusRepository gameBookingStatusRepository,
                                  BookingParticipantRepository bookingParticipantRepository, EmployeeGameInterestRepository employeeGameInterestRepository){
        this.gameBookingRepository =gameBookingRepository;
        this.waitlistRepo=waitlistRepo;
        this.employeeRepository=employeeRepository;
        this.gameTypeRepository=gameTypeRepository;
        this.gameBookingStatusRepository=gameBookingStatusRepository;
        this.bookingParticipantRepository=bookingParticipantRepository;
        this.employeeGameInterestRepository=employeeGameInterestRepository;
    }

    //it, handle game sloat booking request by checking available sloat
    // and in single chain how many times this given host employee play.
    @Override
    @Transactional
    public String attemptBooking(GameBookingRequest gameBookingRequest) {

        Long empId = gameBookingRequest.getEmpId();
        Long gameTypeId = gameBookingRequest.getGameTypeId();
        LocalDateTime requestedSlotStartTime = gameBookingRequest.getRequestedSlotStartTime();
        List<BookingParticipant> bookingParticipants = gameBookingRequest.getBookingParticipants();

        LocalDateTime now = LocalDateTime.now();

        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);

        if(employeeGameInterest == null)
        {
            throw new RuntimeException("host employee has not interest in this game ");
        }

        if (bookingParticipants == null || bookingParticipants.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be null or empty");
        }

        GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
        int gameSlotDuration = gameType.getGameSlotDuration();

        boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(empId, gameTypeId)
                || waitlistRepo.hasAppliedInCycle(empId, gameTypeId)
                || employeeGameInterest.getPlayedInCurrentCycle();

        LocalDateTime sloatEndTime = requestedSlotStartTime.plusMinutes(gameSlotDuration);

        GameBooking gameBookingById = gameBookingRepository.findGameBookingByFkGameType_Id(gameTypeId);

        Employee employee = employeeRepository.findEmployeeById(empId);
        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(3);

        GameBooking saveGameBooking = new GameBooking();

        boolean isSlotFull = gameBookingById.getGameBookingStartTime() == requestedSlotStartTime && gameBookingById.getFkGameBookingStatus().getId() == 3; // "accepted"

        //if given host employee come to book same game in same cycle second time so
        //it not get sloat directly, but it, go in waiting list
        if (isSecondTime) {
            long minutesUntilSlot = Duration.between(now, requestedSlotStartTime).toMinutes();

            if (minutesUntilSlot > 30) {
                addToWaitlist(empId, gameTypeId, requestedSlotStartTime, true, bookingParticipants);
                return "Slot is reserved for first-timers. You have been added to the Priority Waitlist.";
            }
            saveGameBooking.setIsSecondTimePlay(true);
        }

        if (isSlotFull) {
            addToWaitlist(empId, gameTypeId, requestedSlotStartTime, isSecondTime, bookingParticipants);
            return "Slot is full. You are on the waitlist.";
        }

        saveGameBooking.setFkGameBookingStatus(gameBookingStatus);
        saveGameBooking.setGameBookingCreatedAt(Instant.now());
        saveGameBooking.setFkHostEmployee(employee);
        saveGameBooking.setGameBookingStartTime(requestedSlotStartTime);
        saveGameBooking.setGameBookingEndTime(sloatEndTime);
        saveGameBooking.setFkGameType(gameType);
        saveGameBooking.setIsSecondTimePlay(isSecondTime);

        gameBookingRepository.save(saveGameBooking);

        employeeGameInterest.setPlayedInCurrentCycle(true);
        employeeGameInterestRepository.save(employeeGameInterest);

        for (BookingParticipant participant : bookingParticipants) {
            participant.setFkGameBooking(saveGameBooking);

            if (participant.getFkEmployee() == null) {
                throw new IllegalArgumentException("Participant id is required");
            }

            bookingParticipantRepository.save(participant);
        }
        return "Booking confirmed!";
    }

    @Override
    public void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, boolean isSecond, List<BookingParticipant> bookingParticipants) {
        BookingWaitingList bookingWaitingList = new BookingWaitingList();
        Employee employee = employeeRepository.findEmployeeById(empId);
        GameType gameType = gameTypeRepository.findGameTypeById(gameId);
        bookingWaitingList.setFkHostEmployee(employee);
        bookingWaitingList.setFkGameType(gameType);
        bookingWaitingList.setTargetSlotDatetime(slot);
        bookingWaitingList.setIsSecondTimeAttempt(isSecond);
        waitlistRepo.save(bookingWaitingList);

        for (BookingParticipant participant : bookingParticipants) {
            participant.setFkBookingWaitingList(bookingWaitingList);

            if (participant.getFkEmployee() == null) {
                throw new IllegalArgumentException("Participant id is required");
            }

            bookingParticipantRepository.save(participant);
        }
    }

    //update game booking status to cancel (after get sloat I require cancel booking so this method do that)
    @Override
    public GameBooking updateGameBookingStatus(Long pkGameBookingId,Long fkGameStatusId){
        GameBooking gameBooking = gameBookingRepository.findGameBookingById(pkGameBookingId);
        if(fkGameStatusId == 2)
            throw new RuntimeException("played booking cannot be cancel.");
        else if(fkGameStatusId == 3)
            throw new RuntimeException("your status already canceled.");

        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(fkGameStatusId);
        gameBooking.setFkGameBookingStatus(gameBookingStatus);

        return gameBooking;

    }

    @Override
    public List<GameBooking> findAllGameBooking(){
        return gameBookingRepository.findAll();
    }

}
