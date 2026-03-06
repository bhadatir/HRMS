package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.AuthAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthAuditRepository extends JpaRepository<AuthAudit, Long> {
    @Query("""
        select a from AuthAudit a
        where a.userEmail = :email
        and a.logoutTimestamp is null""")
    AuthAudit findAuditAuthByUserEmailAndLogoutTimestampIsNull(String email);
}
