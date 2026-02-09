package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.model.BookingWaitingList;

public interface WaitingListService {
    public BookingWaitingList updateWaitingListStatusById(Long waitingListId);
}
