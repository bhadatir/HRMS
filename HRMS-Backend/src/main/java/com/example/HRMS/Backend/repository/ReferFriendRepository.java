package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.ReferFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferFriendRepository extends JpaRepository<ReferFriend,Long> {
    List<ReferFriend> findReferFriendByFkJob_Id(Long fkJobId);
}
