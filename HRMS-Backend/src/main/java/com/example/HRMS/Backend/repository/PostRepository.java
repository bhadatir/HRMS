package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Post findPostsById(Long id);

    Page<Post> findByPostIsDeletedFalse(Pageable pageable);

    @Query("""
        SELECT p FROM Post p WHERE (
        p.postTitle LIKE %:query%
        OR p.fkPostEmployee.employeeFirstName LIKE %:query%
        OR p.fkPostEmployee.employeeEmail LIKE %:query%
        OR p.fkPostVisibility.postVisibilityName LIKE %:query%
        OR p.postContent LIKE %:query%)
        AND p.postIsDeleted = false
        """)
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);
}
