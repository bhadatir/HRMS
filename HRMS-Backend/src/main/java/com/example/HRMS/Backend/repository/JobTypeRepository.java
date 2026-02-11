package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobTypeRepository extends JpaRepository<JobType,Long> {
    JobType findJobTypesById(Long id);
}
