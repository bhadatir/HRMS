package com.example.HRMS.Backend.repository;

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

    @Query("SELECT j FROM Job j " +
            "WHERE (CAST(j.id AS string) LIKE concat('%', :searchTerm, '%') " +
            "OR lower(j.jobTitle) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR CAST(j.jobSalary AS string) LIKE concat('%', :searchTerm, '%') " +
            "OR lower(j.fkJobType.jobTypeName) LIKE lower(concat('%', :searchTerm, '%')) " +
            "OR CAST(j.jobCreatedAt AS string) LIKE concat('%', :searchTerm, '%') " +
            "OR lower(j.fkJobOwnerEmployee.employeeEmail) LIKE lower(concat('%', :searchTerm, '%'))) " +
            "ORDER BY j.jobCreatedAt DESC")
    Page<Job> findJobBySearchTeam(String searchTerm, Pageable pageable);
}
