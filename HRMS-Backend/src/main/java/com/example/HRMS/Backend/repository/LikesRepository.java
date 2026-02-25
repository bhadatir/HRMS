package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Comment;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Like;
import com.example.HRMS.Backend.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikesRepository extends JpaRepository<Like,Long> {
    List<Like> findLikesByFkPost_Id(Long fkPostId);

    List<Like> findLikesByFkComment_Id(Long fkCommentId);

    void removeLikeByFkCommentAndFkLikeEmployee(Comment fkComment, Employee fkLikeEmployee);

    void removeLikeByFkPostAndFkLikeEmployee(Post fkPost, Employee fkLikeEmployee);

    @Modifying
    @Transactional
    @Query(value = "WITH CommentChain AS (" +
            "SELECT pk_comment_id " +
            "FROM comments " +
            "WHERE pk_comment_id = :commentId " +
            "UNION ALL " +
            "SELECT c.pk_comment_id " +
            "FROM comments c " +
            "INNER JOIN CommentChain cc " +
            "ON c.parent_comment_id = cc.pk_comment_id " +
            ") " +
            "DELETE FROM likes " +
            "WHERE fk_comment_id IN (select pk_comment_id from CommentChain ) ", nativeQuery = true)
    void removeLikesForDeletedComment(Long commentId);

    void removeLikesByFkPost(Post fkPost);
}
