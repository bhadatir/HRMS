package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.dto.NotificationResponse;
import com.example.HRMS.Backend.model.Notification;
import com.example.HRMS.Backend.repository.EmployeeRepository;
import com.example.HRMS.Backend.repository.NotificationRepository;
import com.example.HRMS.Backend.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void markAsSeen(Long empId) {
        notificationRepo.markAllAsRead(empId);
    }

    @Override
    @Transactional
    public void markAsSeenByNotifId(Long notifId){
        notificationRepo.markAsReadById(notifId);
    }

    @Override
    public void createNotification(Long empId, String title, String msg) {
        Notification notification = new Notification();
        notification.setFkEmployee(employeeRepository.findEmployeeById(empId));
        notification.setTitle(title);
        notification.setMessage(msg);
        notification.setRead(false);
        notificationRepo.save(notification);

        messagingTemplate.convertAndSend("/topic/user/" + empId + "/notifications", "NEW_NOTIFICATION");
    }

    @Override
    public List<NotificationResponse> showNotificationByEmployee(Long empId){
        List<Notification> notifications = notificationRepo.findByFkEmployeeIdOrderByCreatedAtDesc(empId);
        List<NotificationResponse> notificationResponses = new ArrayList<>();
        for(Notification notification : notifications)
        {
            NotificationResponse notificationResponse = modelMapper.map(notification,NotificationResponse.class);
            notificationResponses.add(notificationResponse);
        }

        return notificationResponses;
    }


}

