package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.ShareJobData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareJobDataRepository extends JpaRepository<ShareJobData,Long> {
}
