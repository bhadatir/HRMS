package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.PostVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVisibilityRepository extends JpaRepository<PostVisibility,Long> {
    PostVisibility findPostVisibilitiesById(Long id);
}
