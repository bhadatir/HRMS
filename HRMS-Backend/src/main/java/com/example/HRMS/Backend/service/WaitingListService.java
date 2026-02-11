package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.model.BookingWaitingList;

public interface WaitingListService {
    BookingWaitingList updateWaitingListStatusById(Long waitingListId);
}
