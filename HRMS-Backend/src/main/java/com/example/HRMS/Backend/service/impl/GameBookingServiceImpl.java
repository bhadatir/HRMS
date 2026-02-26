package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.*;
import com.example.HRMS.Backend.repository.*;
import com.example.HRMS.Backend.service.DynamicCycleService;
import com.example.HRMS.Backend.service.EmailService;
import com.example.HRMS.Backend.service.GameBookingService;
import com.example.HRMS.Backend.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameBookingServiceImpl implements GameBookingService {

    private final GameBookingRepository gameBookingRepository;

    private final EmailService emailService;

    private final WaitlistRepository waitlistRepository;

    private final DynamicCycleService dynamicCycleService;

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

        GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
        int gameSlotDuration = gameType.getGameSlotDuration();

        LocalDateTime requestedSlotStartTime = gameBookingRequest.getRequestedSlotStartTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sloatEndTime = requestedSlotStartTime.plusMinutes(gameSlotDuration);

        LocalDateTime tomorrow = now.plusDays(1).minusHours(LocalDateTime.now().getHour()).minusMinutes(LocalDateTime.now().getMinute());

        if(requestedSlotStartTime.isBefore(tomorrow) && gameBookingRepository.hasActiveBookingInCycle(empId, gameTypeId)){
            return "Only one active booking allow per game per day.";
        }

        //for game booking
        if (gameBookingRepository.existsOverlappingBooking(gameBookingRequest.getEmpId(), requestedSlotStartTime, sloatEndTime)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have another game booked at this time.");
        }

        if (requestedSlotStartTime.isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book a slot in the past.");
        }

        for (Long participantId : gameBookingRequest.getBookingParticipantsEmpId()) {
            if (gameBookingRepository.existsOverlappingBooking(participantId, requestedSlotStartTime, sloatEndTime)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "One of your participants is already in another game at this time.");
            }
        }

        //for game booking participant
        if (bookingParticipantRepository.existsOverlappingBookingParticipant(gameBookingRequest.getEmpId(), requestedSlotStartTime, sloatEndTime)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have participant in another game booked at this time.");
        }

        for (Long participantId : gameBookingRequest.getBookingParticipantsEmpId()) {
            if (bookingParticipantRepository.existsOverlappingBookingParticipant(participantId, requestedSlotStartTime, sloatEndTime)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "One of your participants is already in another game booking participant at this time.");
            }
        }

        //for game booking waiting list
        if (waitlistRepository.existsOverlappingBookingWaitingList(gameBookingRequest.getEmpId(), requestedSlotStartTime, sloatEndTime)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have waiting in another game at this time.");
        }

        for (Long participantId : gameBookingRequest.getBookingParticipantsEmpId()) {
            if (bookingParticipantRepository.existsOverlappingBookingWaitingListParticipant(participantId, requestedSlotStartTime, sloatEndTime)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "One of your participants is already in another game booking participants waiting list at this time.");
            }
        }

        for (Long participantId : gameBookingRequest.getBookingParticipantsEmpId()) {
            if (waitlistRepository.existsOverlappingBookingWaitingList(participantId, requestedSlotStartTime, sloatEndTime)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "One of your participants is already in another game booking waiting list at this time.");
            }
        }

        List<Long> bookingParticipantsEmpId = gameBookingRequest.getBookingParticipantsEmpId();

        if (bookingParticipantsEmpId.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be null or empty");
        }

        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);

        //requir to check cycle with all diff upcomming cycles.
        boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(empId, gameTypeId)
                || waitlistRepository.hasAppliedInCycle(empId, gameTypeId);

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
                addToWaitlist(empId, gameTypeId, requestedSlotStartTime, sloatEndTime, false, bookingParticipantsEmpId);
                return "Slot is reserved for first-timers. You have been added to the Priority Waitlist.";
            }
        }

        if (isSlotFull) {
            addToWaitlist(empId, gameTypeId, requestedSlotStartTime, sloatEndTime, !isSecondTime, bookingParticipantsEmpId);
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


        List<String> emails = new ArrayList<>();

        for(Long id : bookingParticipantsEmpId){
            Employee employee1 = employeeRepository.findEmployeeById(id);
            BookingParticipant bookingParticipant = new BookingParticipant();
            bookingParticipant.setFkEmployee(employee1);
            bookingParticipant.setFkGameBooking(saveGameBooking);
            bookingParticipantRepository.save(bookingParticipant);

            //if not interested then what ??

            EmployeeGameInterest employeeGameInterest1 = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                    (id, gameType.getId());

            if(employeeGameInterest1 != null) {
                employeeGameInterest1.setPlayedInCurrentCycle(employeeGameInterest1.getPlayedInCurrentCycle() + 1);
                employeeGameInterestRepository.save(employeeGameInterest1);
            }else{
                EmployeeGameInterest employeeGameInterest2 = new EmployeeGameInterest();
                employeeGameInterest2.setFkGameType(gameType);
                employeeGameInterest2.setFkEmployee(employeeRepository.findEmployeeById(id));
                employeeGameInterest2.setPlayedInCurrentCycle(1);
                employeeGameInterestRepository.save(employeeGameInterest2);
            }

            notificationService.createNotification(id
                    ,"Slot Confirmed!"
                    ,  "You have been promoted to your requested slot => " + gameType.getGameName()
                            + " : " + requestedSlotStartTime + " => " + employee.getEmployeeEmail() + " book this sloat."
            );

            emails.add(employee1.getEmployeeEmail());

        }

        emailService.sendEmail(emails,"Slot Confirmed! "
                ,"You Booking is conform for slot => " + gameType.getGameName()
                        + " : " + requestedSlotStartTime + " by your friend : "
                        + employee.getEmployeeEmail() );


        return "Booking confirmed!";
    }

    @Transactional
    @Override
    public void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, LocalDateTime endTime, Boolean isFirstGame, List<Long> bookingParticipantsEmpId) {
        BookingWaitingList bookingWaitingList = new BookingWaitingList();
        Employee employee = employeeRepository.findEmployeeById(empId);
        GameType gameType = gameTypeRepository.findGameTypeById(gameId);
        bookingWaitingList.setFkHostEmployee(employee);
        bookingWaitingList.setFkGameType(gameType);
        bookingWaitingList.setTargetSlotDatetime(slot);
        bookingWaitingList.setTargetSlotEndDatetime(endTime);
        bookingWaitingList.setIsFirstGame(isFirstGame);
        waitlistRepository.save(bookingWaitingList);

        for (Long id : bookingParticipantsEmpId) {

            BookingParticipant participant = new BookingParticipant();
            participant.setFkEmployee(employeeRepository.findEmployeeById(id));
            participant.setFkBookingWaitingList(bookingWaitingList);

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

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void autoUpdateCompletedBookingsAndDeletePastWaitingListEntry() {
        LocalDateTime now = LocalDateTime.now();

        List<BookingWaitingList> expiredBookingWaitingLists = waitlistRepository.findAllByTargetSlotDatetimeBefore(now);

        if (!expiredBookingWaitingLists.isEmpty()) {
            for (BookingWaitingList bookingWaitingList : expiredBookingWaitingLists) {
                List<BookingParticipantResponse> bookingParticipants = bookingParticipantRepository.findAllByBookingWaitingListId(bookingWaitingList.getId());
                for(BookingParticipantResponse res : bookingParticipants){
                    bookingParticipantRepository.removeBookingParticipantById(res.getId());
                }
                waitlistRepository.removeBookingWaitingListById(bookingWaitingList.getId());
            }
        }

        List<GameBooking> expiredBookings = gameBookingRepository
                .findAllByGameBookingEndTimeBeforeAndFkGameBookingStatus_Id(now, 1);

        if (!expiredBookings.isEmpty()) {
            GameBookingStatus completedStatus = gameBookingStatusRepository.findGameBookingStatusById(2);

            for (GameBooking booking : expiredBookings) {
                booking.setFkGameBookingStatus(completedStatus);
            }

            gameBookingRepository.saveAll(expiredBookings);
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void checkSlotAndUpdateBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetSlot = LocalDateTime.now().plusMinutes(31);

        List<GameBooking> upcomingBooking = gameBookingRepository.findAllByGameBookingStartTimeBetween(now, targetSlot);
        if(upcomingBooking == null || upcomingBooking.isEmpty()){
            return;
        }
        for(GameBooking gameBooking: upcomingBooking){
            notificationService.createNotification(gameBooking.getFkHostEmployee().getId()
                    ,"Game Booking Alert!"
                    ,  "You " + gameBooking.getFkGameType().getGameName()
                            + " Game is Start after half hour."
            );
        }

        List<BookingWaitingList> upComingSlots = waitlistRepository.findAllByTargetSlotDatetimeBetween(now, targetSlot);
        if(upComingSlots == null || upComingSlots.isEmpty()){
            return;
        }
        for(BookingWaitingList bookingWaitingList: upComingSlots){
            LocalDateTime targetTime = bookingWaitingList.getTargetSlotDatetime();
            GameType gameType = bookingWaitingList.getFkGameType();

            boolean isSloatEmpty = gameBookingRepository.existsByFkGameTypeAndGameBookingStartTimeAndFkGameBookingStatus_Id(
                    gameType, targetTime, 1);
            if(!isSloatEmpty){
                updateWaitingList(gameType, targetTime);
            }
        }
    }

    @Override
    @Transactional
    public void updateWaitingList(GameType gameType, LocalDateTime targetedSloatTime){

        List<BookingWaitingList> bookingWaitingList = waitlistRepository.findMatchingBookings(gameType.getId(), targetedSloatTime);

        if(bookingWaitingList != null && !bookingWaitingList.isEmpty()) {

            LocalDateTime now = LocalDateTime.now();
            long minutesDiff = Duration.between(now, targetedSloatTime).toMinutes();

            if (bookingWaitingList.get(0).getIsFirstGame() || minutesDiff < 30) {

                addInGameBookingWithNotification(bookingWaitingList.get(0));
            }
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
        gameBooking = gameBookingRepository.save(gameBooking);

        List<BookingParticipant> bookingParticipants = bookingParticipantRepository.findByFkBookingWaitingList_Id(bookingWaitingList.getId());

        for (BookingParticipant bookingParticipant : bookingParticipants) {

            bookingParticipant.setFkBookingWaitingList(null);
            bookingParticipant.setFkGameBooking(gameBooking);

            bookingParticipantRepository.save(bookingParticipant);

            EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                    (bookingParticipant.getFkEmployee().getId(), bookingWaitingList.getFkGameType().getId());

            if(employeeGameInterest != null) {
                employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle() + 1);
                employeeGameInterestRepository.save(employeeGameInterest);
            }else{
                    EmployeeGameInterest employeeGameInterest2 = new EmployeeGameInterest();
                    employeeGameInterest2.setFkGameType(bookingWaitingList.getFkGameType());
                    employeeGameInterest2.setFkEmployee(bookingParticipant.getFkEmployee());
                    employeeGameInterest2.setPlayedInCurrentCycle(1);
                    employeeGameInterestRepository.save(employeeGameInterest2);
                }

            notificationService.createNotification(bookingWaitingList.getFkHostEmployee().getId()
                    ,"Slot Confirmed!"
                    ,  "You have been promoted to your requested slot => " + bookingWaitingList.getFkGameType().getGameName()
                            + " : " + startTime + " => " + bookingWaitingList.getFkHostEmployee().getEmployeeEmail() + " book this sloat."
            );
        }

        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                (bookingWaitingList.getFkHostEmployee().getId(), bookingWaitingList.getFkGameType().getId());
        employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle()+1);
        employeeGameInterestRepository.save(employeeGameInterest);

        waitlistRepository.delete(bookingWaitingList);

        notificationService.createNotification(bookingWaitingList.getFkHostEmployee().getId()
                ,"Slot Confirmed!"
                ,  "You have been promoted to your requested slot => " + bookingWaitingList.getFkGameType().getGameName()
                    + " : " + startTime
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

            List<BookingParticipantResponse> bookingParticipantResponses =
                    bookingParticipantRepository.findAllByGameBookingId(gameBooking.getId());

            gameBookingResponse.setBookingParticipantResponses(bookingParticipantResponses);

            gameBookingResponses.add(gameBookingResponse);
        }
        return gameBookingResponses;
    }

    @Override
    public List<BookingWaitingListResponse> findWaitList(){
        List<BookingWaitingListResponse> bookingWaitingListResponses = new ArrayList<>();
        List<BookingWaitingList> bookingWaitingLists = waitlistRepository.findAll();
        for(BookingWaitingList bookingWaitingList : bookingWaitingLists){
            BookingWaitingListResponse response = modelMapper.map(bookingWaitingList, BookingWaitingListResponse.class);

            List<BookingParticipantResponse> bookingParticipantResponses =
                    bookingParticipantRepository.findAllByBookingWaitingListId(bookingWaitingList.getId());

            response.setBookingParticipantResponses(bookingParticipantResponses);

            bookingWaitingListResponses.add(response);
        }
        return bookingWaitingListResponses;
    }

    @Override
    public BookingWaitingListResponse findWaitListById(Long waitId){
        BookingWaitingList bookingWaitingList = waitlistRepository.findBookingWaitingListsById(waitId);
        BookingWaitingListResponse response = modelMapper.map(bookingWaitingList, BookingWaitingListResponse.class);

        List<BookingParticipantResponse> bookingParticipantResponses =
                bookingParticipantRepository.findAllByBookingWaitingListId(bookingWaitingList.getId());

        response.setBookingParticipantResponses(bookingParticipantResponses);

        return response;
    }

    @Transactional
    @Override
    public void deleteWaitListById(Long waitId){
        List<BookingParticipantResponse> bookingParticipants = bookingParticipantRepository.findAllByBookingWaitingListId(waitId);
        for(BookingParticipantResponse res : bookingParticipants){
            bookingParticipantRepository.removeBookingParticipantById(res.getId());
        }
        waitlistRepository.removeBookingWaitingListById(waitId);
    }

    @Override
    public List<BookingWaitingListResponse> findWaitListSeqByGameTypeAndSloat(Long waitId){
        BookingWaitingList bookingWaitingList = waitlistRepository.findBookingWaitingListsById(waitId);
        List<BookingWaitingList> bookingWaitingLists = waitlistRepository.findMatchingBookings(bookingWaitingList.getFkGameType().getId(),
                bookingWaitingList.getTargetSlotDatetime());
        List<BookingWaitingListResponse> bookingWaitingListResponses = new ArrayList<>();

        for(BookingWaitingList list : bookingWaitingLists){
            BookingWaitingListResponse bookingWaitingListResponse = modelMapper.map(list,BookingWaitingListResponse.class);

            List<BookingParticipantResponse> bookingParticipantResponses =
                    bookingParticipantRepository.findAllByBookingWaitingListId(list.getId());

            bookingWaitingListResponse.setBookingParticipantResponses(bookingParticipantResponses);

            bookingWaitingListResponses.add(bookingWaitingListResponse);
        }
        return bookingWaitingListResponses;
    }

    @Override
    public GameBookingResponse findBookingByEmpId(Long empId)
    {
        return modelMapper.map(
                gameBookingRepository.findGameBookingByFkHostEmployee_Id(empId),
                GameBookingResponse.class);

    }

    @Override
    public void updateGame(Long gameTypeId, GameType gameType){
        GameType gameType1 = gameTypeRepository.findGameTypeById(gameTypeId);
        gameType1.setGameName(gameType.getGameName());
        gameType1.setGameSlotDuration(gameType.getGameSlotDuration());
        gameType1.setOperatingStart(gameType.getOperatingStart());
        gameType1.setOperatingEnd(gameType.getOperatingEnd());
        gameType1.setGameMaxPlayerPerSlot(gameType.getGameMaxPlayerPerSlot());
        gameType1.setLastCycleResetDatetime(LocalDateTime.now());

        LocalTime start = gameType.getOperatingStart().toLocalTime();
        LocalTime end = gameType.getOperatingEnd().toLocalTime();

        long minutes = Duration.between(start, end).toMinutes();

        gameType1.setTotalSlotsPerDay((int) minutes / gameType.getGameSlotDuration());

        gameTypeRepository.save(gameType1);

        dynamicCycleService.performCycleReset(gameType1);

    }

    @Override
    public void addGameInterest(Long empId, Long gameTypeId){
        EmployeeGameInterest existingInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);
        if(existingInterest !=null){
            existingInterest.setInterestDeleted(false);
            employeeGameInterestRepository.save(existingInterest);
        }else {
            Employee employee = employeeRepository.findEmployeeById(empId);
            GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
            EmployeeGameInterest employeeGameInterest = new EmployeeGameInterest();
            employeeGameInterest.setFkEmployee(employee);
            employeeGameInterest.setFkGameType(gameType);
            employeeGameInterestRepository.save(employeeGameInterest);
        }
    }

    @Override
    public void removeGameInterest(Long gameInterestId){
        employeeGameInterestRepository.removeEmployeeGameInterestById(gameInterestId);
    }

    @Override
    public List<EmployeeGameInterestResponse> findGameInterestByEmp(Long empId){

        List<EmployeeGameInterest> employeeGameInterests = employeeGameInterestRepository.getEmployeeGameInterestByFkEmployee_Id(empId);
        List<EmployeeGameInterestResponse> employeeGameInterestResponses =new ArrayList<>();
        for(EmployeeGameInterest employeeGameInterest : employeeGameInterests)
        {
            EmployeeGameInterestResponse employeeGameInterestResponse = modelMapper.map(
                    employeeGameInterest, EmployeeGameInterestResponse.class
            );
            employeeGameInterestResponses.add(employeeGameInterestResponse);
        }
        return employeeGameInterestResponses;
    }

    @Transactional
    @Override
    public void updateGameBooking(Long bookingId, GameBookingRequest gameBookingRequest){

        List<Long> existingEmployeesId = bookingParticipantRepository.findEmployeeIdByGameBookingId(bookingId);

        GameBooking gameBooking = modelMapper.map(gameBookingRequest, GameBooking.class);

        gameBooking.setId(bookingId);

        gameBookingRepository.save(gameBooking);

        List<Long> newEmpId = gameBookingRequest.getBookingParticipantsEmpId();

        List<String> emails = new ArrayList<>();

        for(Long id : newEmpId){
            Employee employee = employeeRepository.findEmployeeById(id);

            if(!existingEmployeesId.contains(id)) {

                BookingParticipant bookingParticipant = new BookingParticipant();

                bookingParticipant.setFkBookingWaitingList(null);
                bookingParticipant.setFkEmployee(employee);
                bookingParticipant.setFkGameBooking(gameBooking);

                bookingParticipantRepository.save(bookingParticipant);

                emails.add(employee.getEmployeeEmail());
                notificationService.createNotification(id, "Game Booking complete", "You are added in Game booking by your friend at :" + Instant.now());

            }
        }

        for(Long id : existingEmployeesId){

            if(!newEmpId.contains(id)) {
                BookingParticipant bookingParticipant = bookingParticipantRepository.findBookingParticipantByEmployeeIdAndGameBookingId(id, bookingId);
                if(bookingParticipant != null) {
                    bookingParticipantRepository.removeBookingParticipantById(bookingParticipant.getId());
                    Employee employee = employeeRepository.findEmployeeById(id);
                    List<String> emails1 = new ArrayList<>();
                    emails1.add(employee.getEmployeeEmail());
                    emailService.sendEmail(emails1, "Remove from booking", "Removed from Game Booking by your friend at :" + Instant.now());
                    notificationService.createNotification(id, "Remove from booking", "Removed from Game Booking by your friend at :" + Instant.now());
                }
            }
        }

        if(!emails.isEmpty()){
            emailService.sendEmail(emails, "Game Booking complete","You are added in Game booking by your friend at :" + Instant.now());
        }
    }

}
