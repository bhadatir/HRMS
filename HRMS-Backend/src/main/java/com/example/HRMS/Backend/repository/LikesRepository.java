package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Comment;
import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.Like;
import com.example.HRMS.Backend.model.Post;
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

    @Modifying
    @Query(value = "DELETE FROM Like l " +
            "WHERE l.fkComment = NULL " +
            "and l.fkPost = :fkPost and l.fkLikeEmployee = :fkLikeEmployee ")
    void removeLikeByFkPostAndFkLikeEmployee(@Param("fkPost") Post fkPost, @Param("fkLikeEmployee") Employee fkLikeEmployee);

}
