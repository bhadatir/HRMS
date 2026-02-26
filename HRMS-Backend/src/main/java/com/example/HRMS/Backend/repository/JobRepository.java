package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.dto.ReferFriendResponse;
import com.example.HRMS.Backend.model.Job;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
    Job findJobByFkJobOwnerEmployee_Id(Long fkJobOwnerEmployeeId);
    Job findJobById(Long id);

    @NotNull(message = "which job you share? that job id is required") Job getJobById(Long id);

}
