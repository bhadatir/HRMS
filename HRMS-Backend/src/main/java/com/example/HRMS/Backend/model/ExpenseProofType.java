package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "expense_proof_type")
public class ExpenseProofType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_expense_proof_type_id", nullable = false)
    private Long id;

    @Size(max = 20)
    @NotNull(message = "expense proof name is required")
    @Column(name = "expense_proof_type_name", nullable = false, length = 20)
    private String expenseProofTypeName;


}