package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByFkEmployeeIdAndIsReadFalse(Long empId);

    List<Notification> findByFkEmployeeIdOrderByCreatedAtDesc(Long empId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.fkEmployee.id = :empId AND n.isRead = false")
    void markAllAsRead(@Param("empId") Long empId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notifId AND n.isRead = false")
    void markAsReadById(@Param("notifId") Long notifId);

}
