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

    @Autowired
    public GameBookingServiceImpl(GameBookingRepository gameBookingRepository,WaitlistRepository waitlistRepo, EmployeeRepository employeeRepository,GameTypeRepository gameTypeRepository, GameBookingStatusRepository gameBookingStatusRepository, BookingParticipantRepository bookingParticipantRepository){
        this.gameBookingRepository =gameBookingRepository;
        this.waitlistRepo=waitlistRepo;
        this.employeeRepository=employeeRepository;
        this.gameTypeRepository=gameTypeRepository;
        this.gameBookingStatusRepository=gameBookingStatusRepository;
        this.bookingParticipantRepository=bookingParticipantRepository;
    }

    @Override
    @Transactional
    public String attemptBooking(GameBookingRequest gameBookingRequest) {

        Long empId = gameBookingRequest.getEmpId();
        Long gameTypeId = gameBookingRequest.getGameTypeId();
        LocalDateTime requestedSlotStartTime = gameBookingRequest.getRequestedSlotStartTime();
        List<BookingParticipant> bookingParticipants = gameBookingRequest.getBookingParticipants();

        LocalDateTime now = LocalDateTime.now();

        if (bookingParticipants == null || bookingParticipants.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be null or empty");
        }

        GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
        LocalDateTime cycleStart = gameType.getCurrentCycleStartDatetime();
        int gameSlotDuration = gameType.getGameSlotDuration();

        boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(empId, gameTypeId, cycleStart) || waitlistRepo.hasAppliedInCycle(empId, gameTypeId, cycleStart);

        LocalDateTime sloatEndTime = requestedSlotStartTime.plusMinutes(gameSlotDuration);

        GameBooking gameBookingById = gameBookingRepository.findGameBookingByFkGameType_Id(gameTypeId);

        Employee employee = employeeRepository.findEmployeeById(empId);
        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(3);

        GameBooking saveGameBooking = new GameBooking();

        boolean isSlotFull = gameBookingById.getGameBookingStartTime() == requestedSlotStartTime && gameBookingById.getFkGameBookingStatus().getId() == 3; // "accepted"

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


//@Scheduled(fixedRate = 60000) // Runs every minute
//public void promoteWaitlist() {
//    LocalDateTime now = LocalDateTime.now();
//    LocalDateTime windowThreshold = now.plusMinutes(30);
//
//    // Find slots starting in exactly 30 minutes that aren't full
//    List<WaitlistEntry> candidates = waitlistRepo.findPromotableEntries(windowThreshold);
//
//    for (WaitlistEntry entry : candidates) {
//        // Double check capacity and promote
//        if (hasCapacity(entry.getSlot(), entry.getGameId())) {
//            convertToBooking(entry);
//        }
//    }
//}