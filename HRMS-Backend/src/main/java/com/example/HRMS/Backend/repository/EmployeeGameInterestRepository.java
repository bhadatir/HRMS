package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.EmployeeGameInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeGameInterestRepository extends JpaRepository<EmployeeGameInterest, Long> {

    EmployeeGameInterest findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(Long fkEmployeeId, Long fkGameTypeId);

    Long countByFkGameTypeId(Long fkGameTypeId);

    List<EmployeeGameInterest> getEmployeeGameInterestByFkGameType_Id(Long fkGameTypeId);

}
