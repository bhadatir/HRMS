package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.Employee;
import com.example.HRMS.Backend.model.TravelDoc;
import com.example.HRMS.Backend.model.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelDocRepository extends JpaRepository<TravelDoc, Long> {
    List<TravelDoc> findTravelDocByFkEmployee_Id(Long fkEmployeeId);


    List<TravelDoc> findByFkEmployeeAndFkTravelPlan(Employee fkEmployee, TravelPlan fkTravelPlan);
}
