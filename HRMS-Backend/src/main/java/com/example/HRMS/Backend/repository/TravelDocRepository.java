package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.TravelDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelDocRepository extends JpaRepository<TravelDoc, Long> {
    List<TravelDoc> findTravelDocByFkEmployee_Id(Long fkEmployeeId);
}
