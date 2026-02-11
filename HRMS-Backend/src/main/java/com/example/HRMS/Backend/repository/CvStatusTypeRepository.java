package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.CvStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvStatusTypeRepository extends JpaRepository<CvStatusType,Long> {
}
