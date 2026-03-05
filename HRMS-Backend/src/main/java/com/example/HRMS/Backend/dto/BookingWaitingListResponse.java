package com.example.HRMS.Backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingWaitingListResponse {

    private Long id;

    private LocalDateTime targetSlotDatetime;

    private LocalDateTime targetSlotEndDatetime;

    private Boolean isFirstGame;

    private Boolean waitingStatusIsActive;

    private LocalDateTime waitingListCreatedAt;

    private Long gameTypeId;

    private String gameTypeName;

    private Integer gameSlotDuration;

    private Long hostEmployeeId;

    private String hostEmployeeEmail;

    private List<BookingParticipantResponse> bookingParticipantResponses;
}
