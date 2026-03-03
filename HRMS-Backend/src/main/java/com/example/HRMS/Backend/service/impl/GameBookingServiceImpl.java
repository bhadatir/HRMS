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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final TravelPlanRepository travelPlanRepository;


    @Override
    @Transactional
    public String attemptBooking(GameBookingRequest request) {
        Long empId = request.getEmpId();
        Long gameTypeId = request.getGameTypeId();
        LocalDateTime startTime = request.getRequestedSlotStartTime();
        List<Long> participantIds = request.getBookingParticipantsEmpId();

        GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
        LocalDateTime endTime = startTime.plusMinutes(gameType.getGameSlotDuration());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        if (startTime.isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book a slot in the past.");
        }
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be empty");
        }

        EmployeeGameInterest hostInterest = validateInterest(empId, gameTypeId);
        for (Long pId : participantIds) {
            validateInterest(pId, gameTypeId);
        }

        List<Long> allInvolved = new ArrayList<>(participantIds);
        allInvolved.add(empId);

        for (Long id : allInvolved) {
            validateNoConflicts(id, startTime, endTime);

            if (startTime.isBefore(tomorrowStart) && gameBookingRepository.hasActiveBookingInCycle(id, gameTypeId)) {
                return "Only one active booking allowed per game per day.";
            }

            boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(id, gameTypeId)
                    || waitlistRepository.hasAppliedInCycle(id, gameTypeId);

            if (isSecondTime && Duration.between(now, startTime).toMinutes() > 30) {
                addToWaitlist(empId, gameTypeId, startTime, endTime, false, participantIds);
                return "Slot is reserved for first-timers. You have been added to the Priority Waitlist.";
            }
        }

        boolean isSecondTime = gameBookingRepository.hasPlayedInCycle(empId, gameTypeId)
                || waitlistRepository.hasAppliedInCycle(empId, gameTypeId);

        boolean isSlotFull = gameBookingRepository.findGameBookingByFkGameType_Id(gameTypeId).stream()
                .anyMatch(b -> b.getGameBookingStartTime().isEqual(startTime) && b.getFkGameBookingStatus().getId() == 1);

        if (isSlotFull) {
            addToWaitlist(empId, gameTypeId, startTime, endTime, !isSecondTime, participantIds);
            return "Slot is full. You are on the waitlist.";
        }

        return finalizeBooking(empId, gameType, startTime, endTime, participantIds, hostInterest);
    }

    private EmployeeGameInterest validateInterest(Long empId, Long gameTypeId) {
        EmployeeGameInterest interest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);
        if (interest == null || interest.isInterestDeleted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employee (ID: " + empId + ") is not interested in this game.");
        }
        return interest;
    }

    private void validateNoConflicts(Long id, LocalDateTime start, LocalDateTime end) {
        if (travelPlanRepository.findAllByTravelStartTimeBetween(id, start.toLocalDate(), end.toLocalDate())) {
            throw new RuntimeException("Employee " + id + " has a travel plan during this period.");
        }
        if (gameBookingRepository.existsOverlappingBooking(id, start, end) ||
                bookingParticipantRepository.existsOverlappingBookingParticipant(id, start, end) ||
                waitlistRepository.existsOverlappingBookingWaitingList(id, start, end) ||
                bookingParticipantRepository.existsOverlappingBookingWaitingListParticipant(id, start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict detected for employee ID: " + id);
        }
    }

    private String finalizeBooking(Long hostId, GameType type, LocalDateTime start, LocalDateTime end, List<Long> pIds, EmployeeGameInterest hostInterest) {
        Employee host = employeeRepository.findEmployeeById(hostId);

        GameBooking booking = new GameBooking();
        booking.setFkHostEmployee(host);
        booking.setFkGameType(type);
        booking.setGameBookingStartTime(start);
        booking.setGameBookingEndTime(end);
        booking.setGameBookingCreatedAt(Instant.now());
        booking.setFkGameBookingStatus(gameBookingStatusRepository.findGameBookingStatusById(1));
        gameBookingRepository.save(booking);

        hostInterest.setPlayedInCurrentCycle(hostInterest.getPlayedInCurrentCycle() + 1);
        employeeGameInterestRepository.save(hostInterest);

        List<String> emails = new ArrayList<>();
        for (Long pId : pIds) {
            Employee p = employeeRepository.findEmployeeById(pId);

            BookingParticipant bp = new BookingParticipant();
            bp.setFkEmployee(p);
            bp.setFkGameBooking(booking);
            bookingParticipantRepository.save(bp);

            EmployeeGameInterest pInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(pId, type.getId());
            pInterest.setPlayedInCurrentCycle(pInterest.getPlayedInCurrentCycle() + 1);
            employeeGameInterestRepository.save(pInterest);

            notificationService.createNotification(pId, "Slot Confirmed!", "Joined " + type.getGameName() + " hosted by " + host.getEmployeeEmail());
            emails.add(p.getEmployeeEmail());
        }

        emailService.sendEmail(emails, "Slot Confirmed!", "Booking confirmed for " + type.getGameName() + " at " + start);
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
    public void updateGameBookingStatus(Long pkGameBookingId, Long fkGameStatusId) {
        GameBooking gameBooking = gameBookingRepository.findGameBookingById(pkGameBookingId);
        if (gameBooking.getFkGameBookingStatus().getId() == 2)
            throw new RuntimeException("played booking cannot be cancel.");
        else if (gameBooking.getFkGameBookingStatus().getId() == 3)
            throw new RuntimeException("your status already canceled.");

        GameBookingStatus gameBookingStatus = gameBookingStatusRepository.findGameBookingStatusById(fkGameStatusId);
        gameBooking.setFkGameBookingStatus(gameBookingStatus);
        gameBookingRepository.save(gameBooking);

        List<BookingParticipant> bookingParticipants = bookingParticipantRepository.findByFkGameBooking_Id(pkGameBookingId);
        for(BookingParticipant bookingParticipant : bookingParticipants){
            bookingParticipantRepository.delete(bookingParticipant);
        }

        if (fkGameStatusId == 3) {
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
                for (BookingParticipantResponse res : bookingParticipants) {
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
        if (upcomingBooking == null || upcomingBooking.isEmpty()) {
            return;
        }
        for (GameBooking gameBooking : upcomingBooking) {
            notificationService.createNotification(gameBooking.getFkHostEmployee().getId()
                    , "Game Booking Alert!"
                    , "You " + gameBooking.getFkGameType().getGameName()
                            + " Game is Start after half hour."
            );
        }

        List<BookingWaitingList> upComingSlots = waitlistRepository.findAllByTargetSlotDatetimeBetween(now, targetSlot);
        if (upComingSlots == null || upComingSlots.isEmpty()) {
            return;
        }
        for (BookingWaitingList bookingWaitingList : upComingSlots) {
            LocalDateTime targetTime = bookingWaitingList.getTargetSlotDatetime();
            GameType gameType = bookingWaitingList.getFkGameType();

            boolean isSloatEmpty = gameBookingRepository.existsByFkGameTypeAndGameBookingStartTimeAndFkGameBookingStatus_Id(
                    gameType, targetTime, 1);
            if (!isSloatEmpty) {
                updateWaitingList(gameType, targetTime);
            }
        }
    }

    @Override
    @Transactional
    public void updateWaitingList(GameType gameType, LocalDateTime targetedSloatTime) {

        List<BookingWaitingList> bookingWaitingList = waitlistRepository.findMatchingBookings(gameType.getId(), targetedSloatTime);

        if (bookingWaitingList != null && !bookingWaitingList.isEmpty()) {

            LocalDateTime now = LocalDateTime.now();
            long minutesDiff = Duration.between(now, targetedSloatTime).toMinutes();

            if (bookingWaitingList.get(0).getIsFirstGame() || minutesDiff < 30) {

                addInGameBookingWithNotification(bookingWaitingList.get(0));
            }
        }
    }

    @Transactional
    public void addInGameBookingWithNotification(BookingWaitingList bookingWaitingList) {

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

            if (employeeGameInterest != null) {
                employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle() + 1);
                employeeGameInterestRepository.save(employeeGameInterest);
            }

            notificationService.createNotification(bookingWaitingList.getFkHostEmployee().getId()
                    , "Slot Confirmed!"
                    , "You have been promoted to your requested slot => " + bookingWaitingList.getFkGameType().getGameName()
                            + " : " + startTime + " => " + bookingWaitingList.getFkHostEmployee().getEmployeeEmail() + " book this sloat."
            );
        }

        EmployeeGameInterest employeeGameInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id
                (bookingWaitingList.getFkHostEmployee().getId(), bookingWaitingList.getFkGameType().getId());
        employeeGameInterest.setPlayedInCurrentCycle(employeeGameInterest.getPlayedInCurrentCycle() + 1);
        employeeGameInterestRepository.save(employeeGameInterest);

        waitlistRepository.delete(bookingWaitingList);

        notificationService.createNotification(bookingWaitingList.getFkHostEmployee().getId()
                , "Slot Confirmed!"
                , "You have been promoted to your requested slot => " + bookingWaitingList.getFkGameType().getGameName()
                        + " : " + startTime
        );

    }

    @Override
    public List<GameBookingResponse> findAllGameBooking() {
        List<GameBooking> gameBookings = gameBookingRepository.findAll();
        List<GameBookingResponse> gameBookingResponses = new ArrayList<>();
        for (GameBooking gameBooking : gameBookings) {
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
    public List<GameBookingResponse> findUpcommingBooking() {
        List<GameBooking> gameBookings = gameBookingRepository.findAllByGameBookingStartTimeBetween(LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(2));
        List<GameBookingResponse> gameBookingResponses = new ArrayList<>();
        for (GameBooking gameBooking : gameBookings) {
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
    public List<BookingWaitingListResponse> findWaitList() {
        List<BookingWaitingListResponse> bookingWaitingListResponses = new ArrayList<>();
        List<BookingWaitingList> bookingWaitingLists = waitlistRepository.findAll();
        for (BookingWaitingList bookingWaitingList : bookingWaitingLists) {
            BookingWaitingListResponse response = modelMapper.map(bookingWaitingList, BookingWaitingListResponse.class);

            List<BookingParticipantResponse> bookingParticipantResponses =
                    bookingParticipantRepository.findAllByBookingWaitingListId(bookingWaitingList.getId());

            response.setBookingParticipantResponses(bookingParticipantResponses);

            bookingWaitingListResponses.add(response);
        }
        return bookingWaitingListResponses;
    }

    @Override
    public List<BookingWaitingListResponse> findWaitListbyEmpId(Long empId) {
        List<BookingWaitingList> bookingWaitingLists = waitlistRepository.findBookingWaitingListsByUser(employeeRepository.findEmployeeById(empId).getId());

        return bookingWaitingLists.stream().map(bookingWaitingList -> {
            return modelMapper.map(bookingWaitingList, BookingWaitingListResponse.class);
        }).toList();
    }

    @Override
    public BookingWaitingListResponse findWaitListById(Long waitId) {
        BookingWaitingList bookingWaitingList = waitlistRepository.findBookingWaitingListsById(waitId);
        BookingWaitingListResponse response = modelMapper.map(bookingWaitingList, BookingWaitingListResponse.class);

        List<BookingParticipantResponse> bookingParticipantResponses =
                bookingParticipantRepository.findAllByBookingWaitingListId(bookingWaitingList.getId());

        response.setBookingParticipantResponses(bookingParticipantResponses);

        return response;
    }

    @Transactional
    @Override
    public void deleteWaitListById(Long waitId) {
        List<BookingParticipantResponse> bookingParticipants = bookingParticipantRepository.findAllByBookingWaitingListId(waitId);
        for (BookingParticipantResponse res : bookingParticipants) {
            bookingParticipantRepository.removeBookingParticipantById(res.getId());
        }
        waitlistRepository.removeBookingWaitingListById(waitId);
    }

    @Override
    public List<BookingWaitingListResponse> findWaitListSeqByGameTypeAndSloat(Long waitId) {
        BookingWaitingList bookingWaitingList = waitlistRepository.findBookingWaitingListsById(waitId);
        List<BookingWaitingList> bookingWaitingLists = waitlistRepository.findMatchingBookings(bookingWaitingList.getFkGameType().getId(),
                bookingWaitingList.getTargetSlotDatetime());
        List<BookingWaitingListResponse> bookingWaitingListResponses = new ArrayList<>();

        for (BookingWaitingList list : bookingWaitingLists) {
            BookingWaitingListResponse bookingWaitingListResponse = modelMapper.map(list, BookingWaitingListResponse.class);

            List<BookingParticipantResponse> bookingParticipantResponses =
                    bookingParticipantRepository.findAllByBookingWaitingListId(list.getId());

            bookingWaitingListResponse.setBookingParticipantResponses(bookingParticipantResponses);

            bookingWaitingListResponses.add(bookingWaitingListResponse);
        }
        return bookingWaitingListResponses;
    }

    @Override
    public Page<GameBookingResponse> findBookingByEmpId(Long empId, String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<GameBooking> gameBookings = gameBookingRepository.findBookingsByUserAndSearch(empId, searchTerm, pageable);

        return gameBookings.map(gameBooking -> {
            GameBookingResponse response = modelMapper.map(gameBooking, GameBookingResponse.class);

            List<BookingParticipantResponse> participants =
                    bookingParticipantRepository.findAllByGameBookingId(gameBooking.getId());

            response.setBookingParticipantResponses(participants);
            return response;
        });
    }

    @Override
    public void updateGame(Long gameTypeId, GameType gameType) {
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
    public void addGameInterest(Long empId, Long gameTypeId) {
        EmployeeGameInterest existingInterest = employeeGameInterestRepository.findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(empId, gameTypeId);
        if (existingInterest != null) {
            existingInterest.setInterestDeleted(false);
            employeeGameInterestRepository.save(existingInterest);
        } else {
            Employee employee = employeeRepository.findEmployeeById(empId);
            GameType gameType = gameTypeRepository.findGameTypeById(gameTypeId);
            EmployeeGameInterest employeeGameInterest = new EmployeeGameInterest();
            employeeGameInterest.setFkEmployee(employee);
            employeeGameInterest.setFkGameType(gameType);
            employeeGameInterestRepository.save(employeeGameInterest);
        }
    }

    @Override
    public void removeGameInterest(Long gameInterestId) {
        employeeGameInterestRepository.removeEmployeeGameInterestById(gameInterestId);
    }

    @Override
    public List<EmployeeGameInterestResponse> findGameInterestByEmp(Long empId) {

        List<EmployeeGameInterest> employeeGameInterests = employeeGameInterestRepository.getEmployeeGameInterestByFkEmployee_Id(empId);
        List<EmployeeGameInterestResponse> employeeGameInterestResponses = new ArrayList<>();
        for (EmployeeGameInterest employeeGameInterest : employeeGameInterests) {
            EmployeeGameInterestResponse employeeGameInterestResponse = modelMapper.map(
                    employeeGameInterest, EmployeeGameInterestResponse.class
            );
            employeeGameInterestResponses.add(employeeGameInterestResponse);
        }
        return employeeGameInterestResponses;
    }

    @Transactional
    @Override
    public void updateGameBooking(Long bookingId, GameBookingRequest gameBookingRequest) {

        List<Long> existingEmployeesId = bookingParticipantRepository.findEmployeeIdByGameBookingId(bookingId);

        GameBooking gameBooking = modelMapper.map(gameBookingRequest, GameBooking.class);

        gameBooking.setId(bookingId);

        gameBookingRepository.save(gameBooking);

        List<Long> newEmpId = gameBookingRequest.getBookingParticipantsEmpId();

        List<String> emails = new ArrayList<>();

        for (Long id : newEmpId) {
            Employee employee = employeeRepository.findEmployeeById(id);

            if (!existingEmployeesId.contains(id)) {

                BookingParticipant bookingParticipant = new BookingParticipant();

                bookingParticipant.setFkBookingWaitingList(null);
                bookingParticipant.setFkEmployee(employee);
                bookingParticipant.setFkGameBooking(gameBooking);

                bookingParticipantRepository.save(bookingParticipant);

                emails.add(employee.getEmployeeEmail());
                notificationService.createNotification(id, "Game Booking complete", "You are added in Game booking by your friend at :" + Instant.now());

            }
        }

        for (Long id : existingEmployeesId) {

            if (!newEmpId.contains(id)) {
                BookingParticipant bookingParticipant = bookingParticipantRepository.findBookingParticipantByEmployeeIdAndGameBookingId(id, bookingId);
                if (bookingParticipant != null) {
                    bookingParticipantRepository.removeBookingParticipantById(bookingParticipant.getId());
                    Employee employee = employeeRepository.findEmployeeById(id);
                    List<String> emails1 = new ArrayList<>();
                    emails1.add(employee.getEmployeeEmail());
                    emailService.sendEmail(emails1, "Remove from booking", "Removed from Game Booking by your friend at :" + Instant.now());
                    notificationService.createNotification(id, "Remove from booking", "Removed from Game Booking by your friend at :" + Instant.now());
                }
            }
        }

        if (!emails.isEmpty()) {
            emailService.sendEmail(emails, "Game Booking complete", "You are added in Game booking by your friend at :" + Instant.now());
        }
    }

    @Override
    public List<SlotAvailabilityResponse> getAvailableSlots(Long gameTypeId, Long employeeId, LocalDate date) {
        GameType game = gameTypeRepository.findGameTypeById(gameTypeId);
        List<SlotAvailabilityResponse> response = new ArrayList<>();

        LocalTime current = game.getOperatingStart().toLocalTime();
        LocalTime end = game.getOperatingEnd().toLocalTime();

        List<GameBooking> dayBookings = gameBookingRepository.findActiveBookingsByDate(date);
        List<BookingWaitingList> dayWaiting = waitlistRepository.findActiveWaitingByDate(date, employeeId);

        while (current.isBefore(end)) {
            LocalDateTime slotStart = LocalDateTime.of(date, current);
            if(slotStart.isAfter(LocalDateTime.now())) {

                LocalDateTime slotEnd = slotStart.plusMinutes(game.getGameSlotDuration());
                String timeStr = current.toString();

                String status = "AVAILABLE";

                boolean isFull = dayBookings.stream().anyMatch(b ->
                        b.getFkGameType().getId().equals(gameTypeId) &&
                                b.getGameBookingStartTime().equals(slotStart));

                boolean isUserInBooking = dayBookings.stream().anyMatch(b -> {
                    boolean isParticipant = bookingParticipantRepository.findByFkGameBooking_Id(b.getId()).stream().anyMatch(p -> p.getFkEmployee().getId().equals(employeeId)) &&
                            (slotStart.isBefore(b.getGameBookingEndTime()) && slotEnd.isAfter(b.getGameBookingStartTime()));
                    boolean isHost = b.getFkHostEmployee().getId().equals(employeeId);

                    boolean overlaps = slotStart.isBefore(b.getGameBookingEndTime()) &&
                            slotEnd.isAfter(b.getGameBookingStartTime());

                    return (isHost || isParticipant) && overlaps;
                });

                boolean isOnTrip = travelPlanRepository.findAllByTravelStartTimeBetween(employeeId, date, date);

                boolean isWaiting = dayWaiting.stream().anyMatch(w ->
                        (slotStart.isBefore(w.getTargetSlotEndDatetime()) && slotEnd.isAfter(w.getTargetSlotDatetime())));

                if (isFull) status = "FULL";
                else if (isUserInBooking || isOnTrip) status = "BUSY";
                else if (isWaiting) status = "WAIT";

                response.add(new SlotAvailabilityResponse(timeStr, status));
            }
            current = current.plusMinutes(game.getGameSlotDuration());
        }
        return response;
    }

}
