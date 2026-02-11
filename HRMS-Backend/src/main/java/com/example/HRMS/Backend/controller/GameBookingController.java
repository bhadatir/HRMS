package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.dto.GameBookingResponse;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.service.GameBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameBookingController {

    private final GameBookingService gameBookingService;

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

    @PutMapping("/status")
    public ResponseEntity<GameBooking> changeGameBookingStatus(@RequestParam Long gameBookingId,@RequestParam Long statusId){
        return ResponseEntity.ok(gameBookingService.updateGameBookingStatus(gameBookingId,statusId));
    }

}
