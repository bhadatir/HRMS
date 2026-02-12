package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.dto.GameBookingResponse;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameBookingServiceImpl implements GameBookingService {

    private final GameBookingRepository gameBookingRepository;

    private final WaitlistRepository waitlistRepo;

    private final ModelMapper modelMapper;

    private final EmployeeRepository employeeRepository;

    private final GameTypeRepository gameTypeRepository;

    private final GameBookingStatusRepository gameBookingStatusRepository;

    private final BookingParticipantRepository bookingParticipantRepository;

    private final EmployeeGameInterestRepository employeeGameInterestRepository;



    //it, handle game sloat booking request by checking available sloat
    // and in single chain how many times this given host employee play.
    @Override
    @Transactional
    public String attemptBooking(GameBookingRequest gameBookingRequest) {

        Long empId = gameBookingRequest.getEmpId();
        Long gameTypeId = gameBookingRequest.getGameTypeId();
        LocalDateTime requestedSlotStartTime = gameBookingRequest.getRequestedSlotStartTime();

        List<BookingParticipant> bookingParticipants = new ArrayList<>();

        System.out.println("hello start");

        List<Long> bookingParticipantsEmpId = gameBookingRequest.getBookingParticipantsEmpId();

//        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);
//
//        if(employeeGameInterest == null)
//        {
//            throw new RuntimeException("host employee has not interest in this game ");
//        }

        if (bookingParticipantsEmpId.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be null or empty");
        }

        GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
        int gameSlotDuration = gameType.getGameSlotDuration();

        //requir to check cycle with all diff upcomming cycles.
        boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(empId, gameTypeId) || waitlistRepo.hasAppliedInCycle(empId, gameTypeId);

        LocalDateTime sloatEndTime = requestedSlotStartTime.plusMinutes(gameSlotDuration);

        List<GameBooking> gameBookingById = gameBookingRepository.findGameBookingByFkGameType_Id(gameTypeId);

        System.out.println("hello" +gameBookingById);

        Employee employee = employeeRepository.findEmployeeById(empId);
        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(1);

        GameBooking saveGameBooking = new GameBooking();

        boolean isSlotFull=false;

        //first time or if sloat is empty then gameBookingById is null so check that
        if(gameBookingById != null) {
            for(GameBooking gameBooking:gameBookingById) {
                isSlotFull = gameBooking.getGameBookingStartTime() == requestedSlotStartTime && gameBooking.getFkGameBookingStatus().getId() == 1; // "accepted"
            }
        }

        System.out.println("hello" +isSlotFull);

        //if given host employee come to book same game in same cycle second time so
        //it not get sloat directly, but it, go in waiting list
        if (isSecondTime) {
            long minutesUntilSlot = Duration.between(LocalDateTime.now(), requestedSlotStartTime).toMinutes();

            if (minutesUntilSlot > 30) {
                addToWaitlist(empId, gameTypeId, requestedSlotStartTime, true, bookingParticipants);
                return "Slot is reserved for first-timers. You have been added to the Priority Waitlist.";
            }
            saveGameBooking.setIsSecondTimePlay(true);
        }


        System.out.println("hello" +isSecondTime);

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

//        employeeGameInterest.setPlayedInCurrentCycle(true);
//        employeeGameInterestRepository.save(employeeGameInterest);

        for(Long id : bookingParticipantsEmpId){
            Employee employee1 = employeeRepository.findEmployeeById(id);
            BookingParticipant bookingParticipant = new BookingParticipant();
            bookingParticipant.setFkEmployee(employee1);
            bookingParticipant.setFkGameBooking(saveGameBooking);
            bookingParticipantRepository.save(bookingParticipant);
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
    public List<GameBookingResponse> findAllGameBooking(){
        List<GameBooking> gameBookings = gameBookingRepository.findAll();
        List<GameBookingResponse> gameBookingResponses = new ArrayList<>();
        for(GameBooking gameBooking :gameBookings)
        {
            GameBookingResponse gameBookingResponse = modelMapper.map(gameBooking,
                    GameBookingResponse.class);
            gameBookingResponses.add(gameBookingResponse);
        }
        return gameBookingResponses;
    }

    @Override
    public GameBookingResponse findBookingByEmpId(Long empId)
    {
        return modelMapper.map(
                gameBookingRepository.findGameBookingByFkHostEmployee_Id(empId),
                GameBookingResponse.class);

    }

}
