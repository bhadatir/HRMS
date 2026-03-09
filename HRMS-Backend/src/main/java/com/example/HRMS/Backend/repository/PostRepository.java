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
    SELECT DISTINCT p FROM Post p
    LEFT JOIN PostTag pt ON pt.fkPost.id = p.id
    LEFT JOIN pt.fkTagType tt
    WHERE (
        CAST(p.id as string) LIKE %:query%
        OR p.postTitle LIKE %:query%
        OR p.fkPostEmployee.employeeFirstName LIKE %:query%
        OR p.fkPostEmployee.employeeEmail LIKE %:query%
        OR p.fkPostVisibility.postVisibilityName LIKE %:query%
        OR p.postContent LIKE %:query%
        OR CAST(p.postCreatedAt as string) LIKE %:query%
        OR tt.tagTypeName LIKE %:query%
    )
    AND (p.fkPostVisibility.postVisibilityName = 'EVERYONE'
        OR p.fkPostVisibility.postVisibilityName = :role
        OR p.fkPostVisibility.postVisibilityName = :position
        OR p.fkPostVisibility.postVisibilityName = :department
        OR p.fkPostEmployee.id = :empId)
    AND p.postIsDeleted = false
    ORDER BY p.postCreatedAt DESC
    """)
    Page<Post> searchPosts(Long empId, @Param("query") String query, String  role, String position, String department, Pageable pageable);
}
