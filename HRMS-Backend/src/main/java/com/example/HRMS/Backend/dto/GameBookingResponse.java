package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.GameBookingStatus;
import com.example.HRMS.Backend.model.GameType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameBookingResponse {

    private Long id;

    private Boolean gameBookingIsDeleted ;

    private LocalDateTime gameBookingStartTime;

    private LocalDateTime gameBookingEndTime;

    private Long gameBookingStatusId;

    private Long gameTypeId;

    private String gameTypeName;

    private Integer gameSlotDuration;

    private Long employeeId;

    private String employeeEmail;

    private Instant gameBookingCreatedAt;

    private List<BookingParticipantResponse> bookingParticipantResponses;

}
