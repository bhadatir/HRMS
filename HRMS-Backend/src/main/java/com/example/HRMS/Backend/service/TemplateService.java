package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.Post;
import com.example.HRMS.Backend.model.ReferFriend;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface TemplateService {
    String generateTravelPlanHtml(TravelPlanRequest request, Long travelPlanId, String subject, String status);

    String generatePostHtml(String content, String mess, String subject);

    String generateGameBookingHtml(GameBooking gameBooking, BookingWaitingList bookingWaitingList, String subject, Boolean isLink);

    String generateJobHtml(ReferFriend referFriend, String cvStatusTypeName, String subject);
}
