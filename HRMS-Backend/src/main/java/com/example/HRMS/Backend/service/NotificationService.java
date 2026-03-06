package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.NotificationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {
    void markAsSeen(Long empId);
    void createNotification(Long empId, String title, String msg);

    Page<NotificationResponse> showNotificationByEmployee(Long empId, String searchTerm, int page, int size);

    void markAsSeenByNotifId(Long notifId);
}
