package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.*;
import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.GameBookingStatus;
import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.repository.GameBookingStatusRepository;
import com.example.HRMS.Backend.repository.WaitlistRepository;
import com.example.HRMS.Backend.service.GameBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameBookingController {

    private final GameBookingService gameBookingService;
    private final WaitlistRepository waitlistRepository;
    private final GameBookingStatusRepository gameBookingStatusRepository;

    @PostMapping("/")
    public ResponseEntity<Object> addGameBooking(@Valid @RequestBody GameBookingRequest gameBookingRequest) {
        return ResponseEntity.ok(gameBookingService.attemptBooking(gameBookingRequest));
    }

    @GetMapping("/")
    public ResponseEntity<List<GameBookingResponse>> showAllBookings(){
        return ResponseEntity.ok(gameBookingService.findAllGameBooking());
    }

    @GetMapping("/{empId}")
    public ResponseEntity<List<GameBookingResponse>> bookingByEmpId(@PathVariable Long empId){
        return ResponseEntity.ok(gameBookingService.findBookingByEmpId(empId));
    }

    @GetMapping("/upcommingBooking")
    public ResponseEntity<List<GameBookingResponse>> upcommingBooking(){
        return ResponseEntity.ok(gameBookingService.findUpcommingBooking());
    }

    @PatchMapping("/status")
    public ResponseEntity<String> changeGameBookingStatus(@RequestParam Long gameBookingId,@RequestParam Long statusId){
        gameBookingService.updateGameBookingStatus(gameBookingId,statusId);
        return ResponseEntity.ok("update status successfully");
    }

    @PutMapping("/booking/{bookingId}")
    public ResponseEntity<String> updateGameBooking(@PathVariable Long bookingId,@RequestBody GameBookingRequest gameBookingRequest){
        gameBookingService.updateGameBooking(bookingId, gameBookingRequest);
        return ResponseEntity.ok("update booking successfully");
    }

    @PostMapping("/interest/{empId}/{gameTypeId}")
    public ResponseEntity<String> addGameInterest(@PathVariable Long empId, @PathVariable Long gameTypeId){
        gameBookingService.addGameInterest(empId, gameTypeId);
        return ResponseEntity.ok("game interest added successfully");
    }

    @PatchMapping("/interest/{gameInterestId}")
    public ResponseEntity<String> removeGameInterest(@PathVariable Long gameInterestId){
        gameBookingService.removeGameInterest(gameInterestId);
        return ResponseEntity.ok("game interest removed successfully");
    }

    @GetMapping("/interest/{empId}")
    public ResponseEntity<List<EmployeeGameInterestResponse>> findGameInterestByEmp(@PathVariable Long empId){
        return ResponseEntity.ok(gameBookingService.findGameInterestByEmp(empId));
    }

    @GetMapping("/waitList/emp/{empId}")
    public ResponseEntity<List<BookingWaitingListResponse>> findWaitListbyEmpId(@PathVariable Long empId){
        return ResponseEntity.ok(gameBookingService.findWaitListbyEmpId(empId));
    }

    @GetMapping("/waitList")
    public ResponseEntity<List<BookingWaitingListResponse>> findWaitList(){
        return ResponseEntity.ok(gameBookingService.findWaitList());
    }

    @GetMapping("/waitList/{waitId}")
    public ResponseEntity<BookingWaitingListResponse> findWaitListById(@PathVariable Long waitId){
        return ResponseEntity.ok(gameBookingService.findWaitListById(waitId));
    }

    @DeleteMapping("/waitList/{waitId}")
    public ResponseEntity<String> deleteWaitListById(@PathVariable Long waitId){
        gameBookingService.deleteWaitListById(waitId);
        return ResponseEntity.ok("waiting list delete successfully");
    }

    @GetMapping("/waitListSeq/{waitId}")
    public ResponseEntity<List<BookingWaitingListResponse>> findWaitListSeqByGameTypeAndSloat(@PathVariable Long waitId){
        return ResponseEntity.ok(gameBookingService.findWaitListSeqByGameTypeAndSloat(waitId));
    }

    @GetMapping("/allGameBookingStatus")
    public ResponseEntity<List<GameBookingStatus>> showAllGameBookingStatus() {
        return ResponseEntity.ok(gameBookingStatusRepository.findAll());
    }

    @GetMapping("/getSlot")
    public ResponseEntity<List<SlotAvailabilityResponse>> getAvailableSlots(@RequestParam Long gameTypeId,
                                                                            @RequestParam Long empId,
                                                                            @RequestParam String date1) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(date1, formatter);
        return ResponseEntity.ok(gameBookingService.getAvailableSlots(gameTypeId, empId, date));
    }



}
