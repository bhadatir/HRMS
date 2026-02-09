package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.GameBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameBookingStatusRepository extends JpaRepository<GameBookingStatus,Long> {
    GameBookingStatus findGameBookingStatusById(long id);
}
