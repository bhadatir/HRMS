package com.example.HRMS.Backend.controller;

import com.example.HRMS.Backend.dto.NotificationResponse;
import com.example.HRMS.Backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/{empId}")
    public ResponseEntity<String> createNotification(@PathVariable Long empId,@RequestParam String title,@RequestParam String message) {
        notificationService.createNotification(empId,title,message);
        return ResponseEntity.ok("notification create successfully");
    }

    @GetMapping("/{empId}")
    public ResponseEntity<Page<NotificationResponse>> showNotificationByEmployee(@PathVariable Long empId,
                                                                                 @RequestParam String searchTerm,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.showNotificationByEmployee(empId, searchTerm, page, size));
    }

    @PostMapping("/markAsSeen/empId/{empId}")
    public ResponseEntity<String> markAsSeen(@PathVariable Long empId){
        notificationService.markAsSeen(empId);
        return ResponseEntity.ok("notification mark As Seen successfully");
    }

    @PostMapping("/markAsSeen/notifId/{notifId}")
    public ResponseEntity<String> markAsSeenByNotifId(@PathVariable Long notifId){
        notificationService.markAsSeenByNotifId(notifId);
        return ResponseEntity.ok("notification mark As Seen successfully");
    }
}
