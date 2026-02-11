package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.ReferFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferFriendRepositort extends JpaRepository<ReferFriend,Long> {
}
