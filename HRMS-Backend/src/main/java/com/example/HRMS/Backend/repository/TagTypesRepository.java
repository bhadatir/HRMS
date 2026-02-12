package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagTypesRepository extends JpaRepository<TagType,Long> {
}
