package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.BookingWaitingListResponse;
import com.example.HRMS.Backend.dto.EmployeeGameInterestResponse;
import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.dto.GameBookingResponse;
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
    public ResponseEntity<GameBookingResponse> bookingByEmpId(@PathVariable Long empId){
        return ResponseEntity.ok(gameBookingService.findBookingByEmpId(empId));
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

    @DeleteMapping("/interest/{gameInterestId}")
    public ResponseEntity<String> removeGameInterest(@PathVariable Long gameInterestId){
        gameBookingService.removeGameInterest(gameInterestId);
        return ResponseEntity.ok("game interest removed successfully");
    }

    @GetMapping("/interest/{empId}")
    public ResponseEntity<List<EmployeeGameInterestResponse>> findGameInterestByEmp(@PathVariable Long empId){
        return ResponseEntity.ok(gameBookingService.findGameInterestByEmp(empId));
    }

    @GetMapping("/waitList")
    public ResponseEntity<List<BookingWaitingListResponse>> findWaitList(){
        return ResponseEntity.ok(gameBookingService.findWaitList());
    }

    @GetMapping("/waitList/{waitId}")
    public ResponseEntity<BookingWaitingListResponse> findWaitListById(@PathVariable Long waitId){
        return ResponseEntity.ok(gameBookingService.findWaitListById(waitId));
    }

    @GetMapping("/waitListSeq/{waitId}")
    public ResponseEntity<List<BookingWaitingListResponse>> findWaitListSeqByGameTypeAndSloat(@PathVariable Long waitId){
        return ResponseEntity.ok(gameBookingService.findWaitListSeqByGameTypeAndSloat(waitId));
    }

    @GetMapping("/allGameBookingStatus")
    public ResponseEntity<List<GameBookingStatus>> showAllGameBookingStatus() {
        return ResponseEntity.ok(gameBookingStatusRepository.findAll());
    }



}
