package com.example.HRMS.Backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class ParticipantsSearch {

    private Long id;

    private String employeeFirstName;

    private String employeeLastName;

    private String employeeEmail;

    private Long roleId;

    private Long positionId;

    private Long departmentId;
}


