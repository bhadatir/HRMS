package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.BookingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingParticipantRepository extends JpaRepository<BookingParticipant,Long> {
}
