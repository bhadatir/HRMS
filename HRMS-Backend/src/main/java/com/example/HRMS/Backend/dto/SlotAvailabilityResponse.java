package com.example.HRMS.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SlotAvailabilityResponse {
    private String time;
    private String status;
}
