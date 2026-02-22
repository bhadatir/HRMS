package com.example.HRMS.Backend.dto;

import com.example.HRMS.Backend.model.BookingWaitingList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingParticipantResponse {
    private Long id;

    private Long bookingWaitingListId;

    private String employeeEmail;
    private String employeeFirstName;
    private String employeeLastName;

}
