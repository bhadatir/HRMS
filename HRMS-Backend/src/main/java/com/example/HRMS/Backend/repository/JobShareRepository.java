package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.JobShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobShareRepository extends JpaRepository<JobShare,Long> {
}
