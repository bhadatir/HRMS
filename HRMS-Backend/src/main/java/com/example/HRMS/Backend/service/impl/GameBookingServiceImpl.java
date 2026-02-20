package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.dto.GameBookingResponse;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.GameBookingService;
import com.example.HRMS.Backend.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameBookingServiceImpl implements GameBookingService {

    private final GameBookingRepository gameBookingRepository;

    private final WaitlistRepository waitlistRepository;

    private final NotificationService notificationService;

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

        List<Long> bookingParticipantsEmpId = gameBookingRequest.getBookingParticipantsEmpId();

        if (bookingParticipantsEmpId.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be null or empty");
        }

        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);

        GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
        int gameSlotDuration = gameType.getGameSlotDuration();

        //requir to check cycle with all diff upcomming cycles.
        boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(empId, gameTypeId)
                || waitlistRepository.hasAppliedInCycle(empId, gameTypeId);

        LocalDateTime sloatEndTime = requestedSlotStartTime.plusMinutes(gameSlotDuration);

        List<GameBooking> gameBookingById = gameBookingRepository.findGameBookingByFkGameType_Id(gameTypeId);


        Employee employee = employeeRepository.findEmployeeById(empId);
        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(1);

        GameBooking saveGameBooking = new GameBooking();

        boolean isSlotFull=false;

        //first time or if sloat is empty then gameBookingById is null so check that
        if(gameBookingById != null) {
            for(GameBooking gameBooking:gameBookingById) {
                if (gameBooking.getGameBookingStartTime().isEqual(requestedSlotStartTime)
                        && gameBooking.getFkGameBookingStatus().getId() == 1) {
                    isSlotFull = true;
                    break;
                }
            }
        }


        //if given host employee come to book same game in same cycle second time so
        //it not get sloat directly, but it, go in waiting list
        if (isSecondTime) {
            long minutesUntilSlot = Duration.between(LocalDateTime.now(), requestedSlotStartTime).toMinutes();

            if (minutesUntilSlot > 30) {
                addToWaitlist(empId, gameTypeId, requestedSlotStartTime, false, bookingParticipants);
                return "Slot is reserved for first-timers. You have been added to the Priority Waitlist.";
            }
        }



        if (isSlotFull) {
            addToWaitlist(empId, gameTypeId, requestedSlotStartTime, !isSecondTime, bookingParticipants);
            return "Slot is full. You are on the waitlist.";
        }

        employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle()+1);
        employeeGameInterestRepository.save(employeeGameInterest);

        saveGameBooking.setFkGameBookingStatus(gameBookingStatus);
        saveGameBooking.setGameBookingCreatedAt(Instant.now());
        saveGameBooking.setFkHostEmployee(employee);
        saveGameBooking.setGameBookingStartTime(requestedSlotStartTime);
        saveGameBooking.setGameBookingEndTime(sloatEndTime);
        saveGameBooking.setFkGameType(gameType);

        gameBookingRepository.save(saveGameBooking);

        for(Long id : bookingParticipantsEmpId){
            Employee employee1 = employeeRepository.findEmployeeById(id);
            BookingParticipant bookingParticipant = new BookingParticipant();
            bookingParticipant.setFkEmployee(employee1);
            bookingParticipant.setFkGameBooking(saveGameBooking);
            bookingParticipantRepository.save(bookingParticipant);
        }

        return "Booking confirmed!";
    }

    @Transactional
    @Override
    public void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, Boolean isFirstGame, List<BookingParticipant> bookingParticipants) {
        BookingWaitingList bookingWaitingList = new BookingWaitingList();
        Employee employee = employeeRepository.findEmployeeById(empId);
        GameType gameType = gameTypeRepository.findGameTypeById(gameId);
        bookingWaitingList.setFkHostEmployee(employee);
        bookingWaitingList.setFkGameType(gameType);
        bookingWaitingList.setTargetSlotDatetime(slot);
        bookingWaitingList.setIsFirstGame(isFirstGame);
        waitlistRepository.save(bookingWaitingList);

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
    @Transactional
    public void updateGameBookingStatus(Long pkGameBookingId,Long fkGameStatusId){
        GameBooking gameBooking = gameBookingRepository.findGameBookingById(pkGameBookingId);
        if(gameBooking.getFkGameBookingStatus().getId() == 2)
            throw new RuntimeException("played booking cannot be cancel.");
        else if(gameBooking.getFkGameBookingStatus().getId() == 3)
            throw new RuntimeException("your status already canceled.");

        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(fkGameStatusId);
        gameBooking.setFkGameBookingStatus(gameBookingStatus);
        gameBookingRepository.save(gameBooking);

        if(fkGameStatusId == 3) {
            updateWaitingList(gameBooking.getFkGameType(), gameBooking.getGameBookingStartTime());
        }

    }

    @Override
    @Transactional
    public void updateWaitingList(GameType gameType, LocalDateTime targetedSloatTime){

        List<BookingWaitingList> bookingWaitingList = waitlistRepository.findMatchingBookings(gameType.getId(), targetedSloatTime);

        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = Duration.between(now, targetedSloatTime).toMinutes();

        if(bookingWaitingList != null && !bookingWaitingList.isEmpty()
                && (bookingWaitingList.get(0).getIsFirstGame() || minutesDiff < 30))
        {
            addInGameBookingWithNotification(bookingWaitingList.get(0));
        }

    }


    @Transactional
    public void addInGameBookingWithNotification(BookingWaitingList bookingWaitingList){

        LocalDateTime startTime = bookingWaitingList.getTargetSlotDatetime();
        LocalDateTime endTime = startTime.plusMinutes(bookingWaitingList.getFkGameType().getGameSlotDuration());

        GameBooking gameBooking = new GameBooking();
        gameBooking.setGameBookingStartTime(startTime);
        gameBooking.setGameBookingEndTime(endTime);
        gameBooking.setFkGameType(bookingWaitingList.getFkGameType());
        gameBooking.setFkGameBookingStatus(gameBookingStatusRepository.findGameBookingStatusById(1));
         gameBooking.setFkHostEmployee(bookingWaitingList.getFkHostEmployee());
        gameBookingRepository.save(gameBooking);

        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                (bookingWaitingList.getFkHostEmployee().getId(), bookingWaitingList.getFkGameType().getId());
        employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle()+1);
        employeeGameInterestRepository.save(employeeGameInterest);

        waitlistRepository.removeBookingWaitingListsById(bookingWaitingList.getId());

        notificationService.createNotification(bookingWaitingList.getFkHostEmployee().getId()
                ,"Slot Confirmed!"
                ,  "You have been promoted to your requested slot."
        );
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


//List<BookingWaitingList> bookingWaitingListSecond = waitlistRepository.findMatchingBookings(gameType.getId(), targetedSloatTime, true);
//
//LocalDateTime now = LocalDateTime.now();
//long minutesDiff = Duration.between(now, targetedSloatTime).toMinutes();
//
//        if(bookingWaitingListFirst != null && !bookingWaitingListFirst.isEmpty()){
//addInGameBookingWithNotification(bookingWaitingListFirst.get(0));
//        }else if(bookingWaitingListSecond != null && minutesDiff < 30 && !bookingWaitingListSecond.isEmpty()){
//addInGameBookingWithNotification(bookingWaitingListSecond.get(0));
//        }