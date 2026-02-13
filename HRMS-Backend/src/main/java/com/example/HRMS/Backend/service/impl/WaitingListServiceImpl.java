package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.model.BookingWaitingList;
import com.example.HRMS.Backend.repository.WaitlistRepository;
import com.example.HRMS.Backend.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingListServiceImpl implements WaitingListService {

    private  final WaitlistRepository waitlistRepository;

    //change waiting list status to isActive = false
    @Override
    public BookingWaitingList updateWaitingListStatusById(Long waitingListId){
        BookingWaitingList bookingWaitingList = waitlistRepository.findBookingWaitingListsById(waitingListId);
        bookingWaitingList.setWaitingStatusIsActive(false);
        return bookingWaitingList;
    }
}
