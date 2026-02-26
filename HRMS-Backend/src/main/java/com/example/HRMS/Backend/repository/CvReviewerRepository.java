package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.CvReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CvReviewerRepository extends JpaRepository<CvReviewer, Long> {
    CvReviewer findCvReviewerById(Long id);

    List<CvReviewer> findCvReviewerByFkJob_Id(Long fkJobId);

    @Query("""
       select count(c) > 0 
       from CvReviewer c 
       where c.fkCvReviewerEmployee.id = :empCvReviewerId 
       and c.fkJob.id = :jobId
       """)
    boolean findCvReviewerIsExist(Long empCvReviewerId, Long jobId);


}
