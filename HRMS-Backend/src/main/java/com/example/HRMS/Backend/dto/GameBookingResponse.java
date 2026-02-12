package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.GameBookingStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class GameBookingResponse {

    private Long id;

    private Boolean gameBookingIsDeleted ;

    private LocalDateTime gameBookingStartTime;

    private LocalDateTime gameBookingEndTime;

    private Boolean isSecondTimePlay;

    private Long gameBookingStatusId;

    private Long gameTypeId;

    private Long employeeId;

    private Instant gameBookingCreatedAt;

}
