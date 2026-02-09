package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.GameBookingRequest;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.GameBookingStatus;
import com.example.HRMS.Backend.model.GameType;
import com.example.HRMS.Backend.service.GameBookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/game")
public class GameBookingController {

    private final GameBookingService gameBookingService;

    @Autowired
    public GameBookingController(GameBookingService gameBookingService)
    {
        this.gameBookingService=gameBookingService;
    }

    @PostMapping("/")
    public ResponseEntity<Object> addGameBooking(@Valid @RequestBody GameBookingRequest gameBookingRequest) {
        return ResponseEntity.ok(gameBookingService.attemptBooking(gameBookingRequest));
    }

    @GetMapping("/")
    public ResponseEntity<List<GameBooking>> showAllBookings()
    {
        return ResponseEntity.ok(gameBookingService.findAllGameBooking());
    }

    @PutMapping("/status")
    public ResponseEntity<GameBooking> changeGameBookingStatus(@RequestParam Long gameBookingId,@RequestParam Long statusId){
        return ResponseEntity.ok(gameBookingService.updateGameBookingStatus(gameBookingId,statusId));
    }

}
