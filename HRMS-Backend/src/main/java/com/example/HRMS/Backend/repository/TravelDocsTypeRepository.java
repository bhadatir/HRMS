package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.TravelDocsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelDocsTypeRepository extends JpaRepository<TravelDocsType, Long> {
    TravelDocsType findTravelDocsTypeById(Long id);
}
