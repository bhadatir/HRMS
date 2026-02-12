package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comment,Long> {
    List<Comment> findCommentByFkCommentEmployee_Id(Long fkCommentEmployeeId);

    List<Comment> findCommentByFkPost_Id(Long fkPostId);

    Comment findCommentById(Long id);
}
