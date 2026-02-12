package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Post findPostsById(Long id);
}
