package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comment,Long> {
    List<Comment> findCommentByFkCommentEmployee_Id(Long fkCommentEmployeeId);

    List<Comment> findCommentByFkPost_Id(Long fkPostId);

    Comment findCommentById(Long id);

    Comment getCommentById(Long id);

    @Modifying
    @Transactional
    @Query(value = "WITH CommentChain AS (" +
            "SELECT pk_comment_id from comments where pk_comment_id = :commentId " +
            "UNION ALL " +
            "SELECT c.pk_comment_id from comments c " +
            "inner join comments cc on c.parent_comment_id = cc.pk_comment_id " +
            ") " +
            "update comments " +
            "set comment_is_deleted = 1 " +
            "where pk_comment_id in (select pk_comment_id from CommentChain ) ", nativeQuery = true)
    void makeCommentIdDeleted(Long commentId);

}
