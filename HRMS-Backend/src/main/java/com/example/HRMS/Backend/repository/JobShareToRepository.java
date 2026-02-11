package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.JobShareTo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobShareToRepository extends JpaRepository<JobShareTo,Long> {
}