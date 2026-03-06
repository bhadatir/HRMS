package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByFkEmployeeIdAndIsReadFalse(Long empId);

    @Query("SELECT n FROM Notification n " +
            "WHERE n.fkEmployee.id = :empId " +
            "AND n.isRead = false " +
            "AND (lower(n.title) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR CAST(n.createdAt AS string) LIKE (concat('%', :searchTerm, '%')) " +
            "OR lower(n.message) LIKE lower(concat('%', :searchTerm, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByFkEmployeeIdOrderByCreatedAtDesc(Long empId, String searchTerm, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.fkEmployee.id = :empId AND n.isRead = false")
    void markAllAsRead(@Param("empId") Long empId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notifId AND n.isRead = false")
    void markAsReadById(@Param("notifId") Long notifId);

}
