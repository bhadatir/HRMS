package com.example.HRMS.Backend.repository;

import com.example.HRMS.Backend.model.EmployeeGameInterest;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeGameInterestRepository extends JpaRepository<EmployeeGameInterest, Long> {

    EmployeeGameInterest findEmployeeGameInterestByFkEmployee_IdAndFkGameType_Id(Long fkEmployeeId, Long fkGameTypeId);

    Long countByFkGameTypeId(Long fkGameTypeId);

    List<EmployeeGameInterest> getEmployeeGameInterestByFkGameType_Id(Long fkGameTypeId);
    List<EmployeeGameInterest> getEmployeeGameInterestByFkEmployee_Id(Long fkGameTypeId);

    @Modifying
    @Transactional
    @Query(value = "update EmployeeGameInterest e " +
            "set e.isInterestDeleted = true " +
            "where e.id = :gameInterestId ")
    void removeEmployeeGameInterestById(Long gameInterestId);
}
