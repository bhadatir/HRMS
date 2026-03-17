package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.TravelPlanRequest;
import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.model.GameBooking;
import com.example.HRMS.Backend.model.Post;
import com.example.HRMS.Backend.model.ReferFriend;
import com.example.HRMS.Backend.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String generateTravelPlanHtml(TravelPlanRequest request, Long travelPlanId, String subject, String status) {

        String link = "http://localhost:5173/travel-plan?travelPlanId=" + travelPlanId;

        Context context = new Context();

        context.setVariable("subject", subject);
        context.setVariable("planName", request.getTravelPlanName());
        context.setVariable("startDate", request.getTravelPlanStartDate());
        context.setVariable("details", request.getTravelPlanDetails());
        context.setVariable("link", link);

        if(status != null) context.setVariable("status", status);

        context.setVariable("currentDate", LocalDate.now());
        context.setVariable("currentTime", LocalTime.now().withNano(0));

        return templateEngine.process("travel-plan-notification", context);
    }

    @Override
    public String generateGameBookingHtml(GameBooking gameBooking, BookingWaitingList bookingWaitingList, String subject, Boolean isLink){

        Context context = new Context();
        String link = "http://localhost:5173/game-management?gameBookingId=" + gameBooking.getId();

        if(gameBooking.getId() != null) {
            context.setVariable("gameName", gameBooking.getFkGameType().getGameName());
            context.setVariable("slotTime", gameBooking.getGameBookingStartTime());
            if(Boolean.TRUE.equals(isLink)) context.setVariable("link", link);
        }

        if(bookingWaitingList.getId() != null) {
            context.setVariable("gameName", bookingWaitingList.getFkGameType().getGameName());
            context.setVariable("slotTime", bookingWaitingList.getTargetSlotDatetime());
        }

        context.setVariable("subject", subject);
        context.setVariable("currentDate", LocalDate.now());
        context.setVariable("currentTime", LocalTime.now().withNano(0));

        return templateEngine.process("game-booking-notification", context);
    }

    @Override
    public String generatePostHtml(String content, String mess, String subject){
        Context context = new Context();

        context.setVariable("message",mess);
        context.setVariable("subject", subject);
        context.setVariable("content", content);
        context.setVariable("currentDate", LocalDate.now());
        context.setVariable("currentTime", LocalTime.now().withNano(0));

        return templateEngine.process("post-notification", context);
    }

    @Override
    public String generateJobHtml(ReferFriend referFriend, String cvStatusTypeName, String subject){
        Context context = new Context();
        String link = "http://localhost:5173/job-management?jobId=" + referFriend.getFkJob().getId();

        context.setVariable("subject", subject);
        context.setVariable("jobName",referFriend.getFkJob().getJobTitle());
        context.setVariable("status", cvStatusTypeName);
        context.setVariable("link", link);
        context.setVariable("currentDate", LocalDate.now());
        context.setVariable("currentTime", LocalTime.now().withNano(0));

        return templateEngine.process("job-notification", context);
    }

}
