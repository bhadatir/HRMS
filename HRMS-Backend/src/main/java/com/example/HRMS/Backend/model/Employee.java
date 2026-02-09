package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Data
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_employee_id", nullable = false)
    private Long id;

    @Size(max = 25)
    @NotNull(message = "Employee first name is required")
    @Column(name = "employee_first_name", nullable = false)
    private String employeeFirstName;

    @Size(max = 25)
    @NotNull(message = "Employee last name is required")
    @Column(name = "employee_last_name", nullable = false)
    private String employeeLastName;

    @Size(max = 25)
    @NotNull(message = "Employee email address is required")
    @Email(message = "Email is not in perfect formate")
    @Column(name = "employee_email", nullable = false)
    private String employeeEmail;

    @Size(max = 100)
    @NotNull(message = "Employee password is required")
    @Column(name = "employee_password", nullable = false)
    private String employeePassword;

    @PastOrPresent(message = "DOB cannot be in the future")
    @NotNull(message = "Employee DOB is required")
    @Column(name = "employee_dob", nullable = false)
    private LocalDate employeeDob;

    @NotNull(message = "Employee Gender is required")
    @Column(name = "employee_gender", nullable = false)
    private String employeeGender;

    @NotNull(message = "Employee hire date is required")
    @Column(name = "employee_hire_date", nullable = false)
    private LocalDate employeeHireDate;

    @NotNull(message = "Employee salary is required")
    @Min(value = 0,message = "Salary cannot be negative")
    @Column(name = "employee_salary", nullable = false)
    private Integer employeeSalary;

    @Column(name = "employee_is_active")
    private Boolean employeeIsActive = true;

    @NotNull(message = "Employee department id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_department_id", nullable = false)
    private Department fkDepartment;

    @NotNull(message = "Employee position id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_position_id", nullable = false)
    private Position fkPosition;

    @NotNull(message = "Employee role id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_role_id", nullable = false)
    private Role fkRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_manager_employee_id")
    private Employee fkManagerEmployee;

}



//[08/02, 6:21 pm] Tirth Bhadani: To help you visualize how this complex fairness logic works in the real world, here are four distinct scenarios based on the 12-hour cycle and the 30-minute lock-out rule.Scenario 1: The First-Timer (Priority Access)Context: It is 9:00 AM. A 12-hour cycle has just started. Employee A (has not played yet) wants to book the 4:00 PM slot. * Request: Employee A selects the 4:00 PM Pool slot. * System Check: has_played_in_current_cycle is 0. * Action: System grants the booking immediately. * Result: game_booking is updated. employee_game_interest.has_played_in_current_cycle is set to 1. * Outcome: Employee A is guaranteed to play.Scenario 2: The Second-Timer (The Waitlist Hold)Context: It is 9:15 AM. Employee A (who just booked for 4:00 PM) now wants to book a second game at 5:00 PM. * Request: Employee A selects 5:00 PM. * System Check: has_played_in_current_cycle is 1. * Action: Because the slot is more than 30 minutes away, the system blocks a direct booking. * Queue: Employee A is placed in booking_waiting_list with is_second_time_attempt = 1. * Outcome: Employee A must wait to see if any "First-Timer" takes that slot before 4:30 PM.Scenario 3: The 30-Minute "Last Call" (Promotion)Context: It is now 4:31 PM. The 5:00 PM slot from Scenario 2 is still empty. No First-Timer booked it. * System Trigger: The 30-minute window for the 5:00 PM slot has opened. * Check Queue: The system finds Employee A in the booking_waiting_list marked as a "Second-Timer." * Action: Since no First-Timers are waiting, the system promotes Employee A. * Result: Employee A receives an automated notification: "You've been promoted to the 5:00 PM slot!" * Outcome: The slot is filled, ensuring the table doesn't sit idle.Scenario 4: The Cancellation & "First-Timer" JumpContext: It is 10:00 AM. Employee B (First-Timer) and Employee C (Second-Timer) are both on the waitlist for a 2:00 PM slot that is currently full. * Event: A current player in the 2:00 PM slot cancels at 10:30 AM. * Trigger: The system looks at the booking_waiting_list for 2:00 PM. * Sorting Logic:   * Employee B (Score: 0 / First-Timer)   * Employee C (Score: 1 / Second-Timer) * Action: The system promotes Employee B immediately, even if Employee C joined the waitlist earlier. * Outcome: The "Fairness Rule" is maintained; the person who hasn't played yet jumps to the front of the line.Summary Table: Who Gets the Slot?| Requester | Current Time | Slot Time | Time Diff | Result ||---|---|---|---|---|| First-Timer | 8:00 AM | 5:00 PM | 9 hrs | Booked Instantly || Second-Timer | 8:00 AM | 5:00 PM | 9 hrs | Waitlisted || Second-Timer | 4:35 PM | 5:00 PM | 25 mins | Booked (Window Open) || First-Timer | 4:40 PM | 5:00 PM | 20 mins | Booked (Always Priority) |Final Database View for HRTo manage this, HR would use a query like this to see the "Queue Health":SELECT     target_slot_datetime,    COUNT(CASE WHEN is_second_time_attempt = 0 THEN 1 END) as waiting_first_timers,    COUNT(CASE WHEN is_second_time_attempt = 1 THEN 1 END) as waiting_second_timersFROM booking_waiting_listGROUP BY target_slot_datetimeORDER BY target_slot_datetime ASC;
//Would you like me to generate the SQL code for the "Nightly Reset" procedure that clears the has_played flags and archives the day's games?
//
// [08/02, 6:21 pm] Tirth Bhadani: This logic introduces a Lock-Out Window. Essentially, the first 11.5 hours of a 12-hour cycle belong exclusively to "First-Timers." A "Second-Timer" can only claim a slot if it remains vacant and the clock is within 30 minutes of the slot starting.To handle this, we need to track the Cycle Start Time and use a "Waitlist-to-Booking" conversion logic that respects this 30-minute buffer.1. The Core Logic * Fixed Cycle: If you have 24 slots (12 hours) and 96 interested employees, and each slot holds 4 people (24 \times 4 = 96), the cycle perfectly resets every 12 operating hours. * The 30-Minute Grace Period: * If a "First-Timer" wants a slot, they can book it anytime.   * If a "Second-Timer" wants it, they are placed in a "Holding Queue".   * The system only moves them from the queue to an active booking if the current time is \le 30 minutes before the slot starts AND no First-Timer has claimed it.2. Final Optimized Database StructureA. Game & Cycle ConfigurationCREATE TABLE game_type (    pk_game_type_id INT IDENTITY(1,1) PRIMARY KEY,    game_name VARCHAR(50) NOT NULL,    operating_start TIME NOT NULL, -- e.g., '08:00'    operating_end TIME NOT NULL, -- e.g., '20:00' (12 hours)    slot_duration_minutes INT NOT NULL DEFAULT 30,    max_players_per_slot INT NOT NULL DEFAULT 4,    -- Current cycle start (e.g., Feb 8, 2026 08:00 AM)    current_cycle_start_datetime DATETIME NOT NULL );
//CREATE TABLE employee_game_interest (    fk_employee_id INT NOT NULL,    fk_game_type_id INT NOT NULL,    -- Tracks if they have used their 'First Timer' ticket in the CURRENT cycle    has_played_in_current_cycle BIT DEFAULT 0,     PRIMARY KEY (fk_employee_id, fk_game_type_id),    FOREIGN KEY (fk_employee_id) REFERENCES employee(pk_employee_id),    FOREIGN KEY (fk_game_type_id) REFERENCES game_type(pk_game_type_id));
//B. Bookings & The Priority QueueCREATE TABLE game_booking (    pk_game_booking_id INT IDENTITY(1,1) PRIMARY KEY,    fk_game_type_id INT NOT NULL,    fk_host_employee_id INT NOT NULL,    slot_start_datetime DATETIME NOT NULL,    slot_end_datetime DATETIME NOT NULL,    is_second_time_play BIT DEFAULT 0, -- Flag to identify if this was a 30-min-window booking    status VARCHAR(20) DEFAULT 'Scheduled',    FOREIGN KEY (fk_game_type_id) REFERENCES game_type(pk_game_type_id),    FOREIGN KEY (fk_host_employee_id) REFERENCES employee(pk_employee_id));
//CREATE TABLE booking_waiting_list (    pk_waiting_id INT IDENTITY(1,1) PRIMARY KEY,    fk_game_type_id INT NOT NULL,    fk_employee_id INT NOT NULL,    target_slot_datetime DATETIME NOT NULL,    is_second_time_attempt BIT NOT NULL, -- True if they already played this cycle    created_at DATETIME DEFAULT GETDATE(),    FOREIGN KEY (fk_game_type_id) REFERENCES game_type(pk_game_type_id),    FOREIGN KEY (fk_employee_id) REFERENCES employee(pk_employee_id));
//        3. Implementation of the "30-Minute Rule"You don't need a complex trigger for the 30-minute rule; you need a Conditional Selection Logic in your application or a Stored Procedure.The Logic for a Second-Timer (Employee #1): * Employee #1 tries to book the 11:00 AM slot at 9:00 AM. * System checks has_played_in_current_cycle. It is 1 (True). * System checks the clock. It is 2 hours before the slot. * Action: System inserts Employee #1 into booking_waiting_list with is_second_time_attempt = 1. * The Window Opens: At 10:30 AM (30 mins before), a background job or the next person checking slots runs this query:<!-- end list -->-- Promote Second-Timers only if slot is still empty 30 mins beforeIF (DATEDIFF(MINUTE, GETDATE(), @SlotStartTime) <= 30)BEGIN    INSERT INTO game_booking (...)    SELECT TOP 1 fk_employee_id, ...    FROM booking_waiting_list    WHERE target_slot_datetime = @SlotStartTime    AND is_second_time_attempt = 1    ORDER BY created_at ASC; -- First come, first served for second-timersEND
//        4. Summary of the Cycle ResetTo handle the "12-hour reset," you should have a scheduled task (Cron job) that runs every night or at the end of the operating shift: * Reset Interests: UPDATE employee_game_interest SET has_played_in_current_cycle = 0; * Advance Cycle: UPDATE game_type SET current_cycle_start_datetime = DATEADD(HOUR, 12, current_cycle_start_datetime); * Clear Old Queue: Delete any remaining entries in the booking_waiting_list from the previous cycle.Why this works: * Fairness: First-timers get 11.5 hours of exclusive booking time. * Efficiency: Slots don't go to waste. If no first-timer wants it by the 30-minute mark, the second-timer (who is already waiting in the queue) gets it automatically. * Organization: The is_second_time_play flag helps HR audit who is using the "extra" slots.Would you like me to create the SQL view that shows HR exactly how many "First-Timer" slots are still available for the current 12-hour cycle?
//
//        [08/02, 6:26 pm] Tirth Bhadani: This is a classic "loophole" in fairness systems. To stop users from bypassing the rules by simply swapping who clicks the "Book" button, the system must track participation, not just ownership.To fix this, the "Has Played" status must update for all 4 people in the slot, and the 30-minute rule must check the status of the person trying to book.1. The Logic Update * The Check: When Employee B tries to book a slot and add Employee A as a friend, the system checks the "Has Played" status for both of them. * The Constraint: If any person in the group (the booker or the invited friends) has already played in the current cycle, the entire booking is treated as a "Second-Time Play" and is pushed to the 30-minute waiting queue. * The Update: Once a game is completed, the system marks the has_played flag for the Host and all 3 Participants.2. Final Optimized Database StructureA. Updated Interest TableWe keep this simple but ensure it is the "Source of Truth" for everyone's status.CREATE TABLE employee_game_interest (    fk_employee_id INT NOT NULL,    fk_game_type_id INT NOT NULL,    has_played_in_current_cycle BIT DEFAULT 0, -- Set to 1 for Host AND Friends after a game    PRIMARY KEY (fk_employee_id, fk_game_type_id));
//B. The "Group Validation" logicWe don't need to change the table structure much, but we change how we validate a booking. When a user submits a booking with friends, your backend must run this check:-- Check if ANYONE in the proposed group has played alreadySELECT COUNT(*) FROM employee_game_interest WHERE fk_game_type_id = @SelectedGame   AND has_played_in_current_cycle = 1  AND fk_employee_id IN (@BookerID, @Friend1, @Friend2, @Friend3);
//         * Result = 0: Group is "Pure" (All First-Timers). Instant Booking. * Result > 0: Group is "Mixed" or "Second-Timers." Sent to Waiting List (30-min rule).3. Scenario Example: The "Friend Swap" FixThe Situation: * Slot 1 (9:00 AM): Employee A books and invites Employee B. They play. * The Update: The system sets has_played = 1 for both A and B. * Slot 2 (1:00 PM): Employee B tries to book and invites Employee A (The Swap).The System Response: * The system checks the list (B, A). * It sees both have has_played = 1. * Action: It refuses the 1:00 PM booking and puts them on the Waiting List because they are now "Second-Timers." * Outcome: They can only get that 1:00 PM slot if no one else wants it by 12:30 PM.4. Handling the "Add Friends" Step in the TableTo ensure the friends are also tracked for the 30-minute rule, we link them to the waitlist too.CREATE TABLE booking_participants (    pk_participant_id INT IDENTITY(1,1) PRIMARY KEY,    fk_game_booking_id INT NULL, -- Link here if slot is confirmed    fk_waiting_id INT NULL, -- Link here if slot is in waitlist    fk_employee_id INT NOT NULL,    FOREIGN KEY (fk_employee_id) REFERENCES employee(pk_employee_id));
//Summary of the Final Fairness Rules * Group Priority: A group is only as fast as its "slowest" member. If you invite one person who has already played, your whole group becomes a "Second-Timer" group. * No Loopholes: Swapping the "Host" role no longer works because the has_played flag tracks every individual's participation. * Automatic Reset: Every 12 hours, the system runs UPDATE employee_game_interest SET has_played_in_current_cycle = 0, and the next 96-person cycle begins.Would you like me to write the stored procedure that performs the "Group Check" and automatically decides whether to put the request in game_booking or booking_waiting_list?













//To implement this complex "Fairness & 30-Minute Window" logic in Spring Boot, we will use a combination of Spring Data JPA for database interaction and a Service Layer to handle the business logic (the priority checks).1. Project DependenciesYou will need the following in your pom.xml: * spring-boot-starter-data-jpa * spring-boot-starter-web * mysql-connector-java (or your preferred DB driver)2. The Entity Model (Example: GameBooking)We will focus on the core logic. Assume the Employee and GameType entities exist.@Entity@Table(name = "game_booking")public class GameBooking {    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private Long pkGameBookingId;
//    @ManyToOne    @JoinColumn(name = "fk_host_employee_id")    private Employee host;
//    @ManyToOne    @JoinColumn(name = "fk_game_type_id")    private GameType gameType;
//    private LocalDateTime slotStartDatetime;    private LocalDateTime slotEndDatetime;    private boolean isSecondTimePlay;    private String status = "Scheduled";    private boolean isDeleted = false;        // Getters and Setters}
//            3. The Repository LayerWe need a custom query to check if the employee has already played in the current 12-hour cycle.@Repositorypublic interface GameBookingRepository extends JpaRepository<GameBooking, Long> {        // Check if employee has an active/completed booking in the current 12-hour window    @Query("SELECT COUNT(g) > 0 FROM GameBooking g WHERE g.host.id = :empId " +           "AND g.gameType.id = :gameId AND g.isDeleted = false " +           "AND g.slotStartDatetime >= :cycleStart")    boolean hasPlayedInCycle(@Param("empId") Long empId,                              @Param("gameId") Long gameId,                              @Param("cycleStart") LocalDateTime cycleStart);
//                // Count current occupants of a specific slot    long countBySlotStartDatetimeAndGameTypeIdAndIsDeletedFalse(LocalDateTime start, Long gameId);}
//            4. The Service Layer (The Logic Engine)This is where the 30-minute rule and Waitlist logic live.@Servicepublic class GameSchedulingService {
//    @Autowired    private GameBookingRepository bookingRepo;        @Autowired    private WaitlistRepository waitlistRepo;
//    @Transactional    public String attemptBooking(Long empId, Long gameId, LocalDateTime requestedSlot) {        LocalDateTime now = LocalDateTime.now();                // 1. Calculate the start of the current 12-hour cycle (e.g., 8 AM or 8 PM)        LocalDateTime cycleStart = determineCycleStart(now);                // 2. Check if this is a first-time or second-time play        boolean isSecondTime = bookingRepo.hasPlayedInCycle(empId, gameId, cycleStart);                // 3. Check Slot Capacity        long currentPlayers = bookingRepo.countBySlotStartDatetimeAndGameTypeIdAndIsDeletedFalse(requestedSlot, gameId);        boolean isSlotFull = currentPlayers >= 4; // Max players
//        // 4. APPLY THE LOGIC        if (isSecondTime) {            long minutesUntilSlot = Duration.between(now, requestedSlot).toMinutes();                        if (minutesUntilSlot > 30) {                // Too early for a second-timer -> Send to Waitlist                addToWaitlist(empId, gameId, requestedSlot, true);                return "Slot is reserved for first-timers. You have been added to the Priority Waitlist.";            }        }
//        if (isSlotFull) {            addToWaitlist(empId, gameId, requestedSlot, isSecondTime);            return "Slot is full. You are on the waitlist.";        }
//        // 5. Successful Booking        saveBooking(empId, gameId, requestedSlot, isSecondTime);        return "Booking confirmed!";    }
//    private void addToWaitlist(Long empId, Long gameId, LocalDateTime slot, boolean isSecond) {        BookingWaitingList entry = new BookingWaitingList();        entry.setEmployeeId(empId);        entry.setGameTypeId(gameId);        entry.setTargetSlotDatetime(slot);        entry.setSecondTimeAttempt(isSecond);        waitlistRepo.save(entry);    }}
//5. Automation: The 30-Minute "Promotion" TaskYou can use @Scheduled in Spring to check the waitlist every minute and promote "Second-Timers" into empty slots once the 30-minute window opens.@Scheduled(fixedRate = 60000) // Runs every minutepublic void promoteWaitlist() {    LocalDateTime now = LocalDateTime.now();    LocalDateTime windowThreshold = now.plusMinutes(30);
//    // Find slots starting in exactly 30 minutes that aren't full    List<WaitlistEntry> candidates = waitlistRepo.findPromotableEntries(windowThreshold);
//            for (WaitlistEntry entry : candidates) {        // Double check capacity and promote        if (hasCapacity(entry.getSlot(), entry.getGameId())) {            convertToBooking(entry);        }    }}
//            6. Summary of API Endpoints * POST /api/games/book: Takes employeeId, gameTypeId, and slotTime. Returns status (Confirmed/Waitlisted). * GET /api/games/slots: Returns all slots for the day with a "Status" (Available, Full, or Locked for First-Timers). * DELETE /api/games/cancel/{id}: Triggers the promotion of the next person in the queue.Would you like me to provide the SQL query for the findPromotableEntries method to handle the sorting of First-Timers vs. Second-Timers?