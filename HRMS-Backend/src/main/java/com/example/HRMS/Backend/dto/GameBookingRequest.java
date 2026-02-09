package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.BookingParticipant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameBookingRequest {
    @NotNull(message = "Employee Id is required")
    private Long empId;

    @NotNull(message = "GameType Id is required")
    private Long gameTypeId;

    @NotNull(message = "Sloat start Date and Time is required")
    private LocalDateTime requestedSlotStartTime;

    @NotNull(message = "Participants are required")
    List<BookingParticipant> bookingParticipants;

}
