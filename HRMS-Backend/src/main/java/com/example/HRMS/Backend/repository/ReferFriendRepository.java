package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.dto.ReferFriendResponse;
import com.example.HRMS.Backend.model.CvStatusType;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Job;
import com.example.HRMS.Backend.model.ReferFriend;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReferFriendRepository extends JpaRepository<ReferFriend,Long> {
    List<ReferFriend> findReferFriendByFkJob_Id(Long fkJobId);

}
