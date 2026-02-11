package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.CvReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvReviewerRepository extends JpaRepository<CvReviewer, Long> {
    CvReviewer findCvReviewerById(Long id);
}
