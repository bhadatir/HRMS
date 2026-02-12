package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikesRepository extends JpaRepository<Like,Long> {
    List<Like> findLikesByFkPost_Id(Long fkPostId);

    List<Like> findLikesByFkComment_Id(Long fkCommentId);
}
