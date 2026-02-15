package com.example.HRMS.Backend.service;

import com.example.HRMS.Backend.dto.NotificationResponse;
import com.example.HRMS.Backend.model.Notification;

import java.util.List;

public interface NotificationService {
    void markAsSeen(Long empId);
    void createNotification(Long empId, String title, String msg);

    List<NotificationResponse> showNotificationByEmployee(Long empId);

    void markAsSeenByNotifId(Long notifId);
}
