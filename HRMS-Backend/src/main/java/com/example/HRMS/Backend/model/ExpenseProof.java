package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "expense_proof")
public class ExpenseProof {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_expense_proof_id", nullable = false)
    private Integer id;

    @ColumnDefault("getdate()")
    @Column(name = "expense_proof_uploaded_at")
    private Instant expenseProofUploadedAt;

    @Size(max = 255)
    @NotNull(message = "expense proof doc/img is required")
    @Column(name = "expense_proof_url", nullable = false)
    private String expenseProofUrl;

    @NotNull(message = "expense id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_expense_id", nullable = false)
    private Expense fkExpense;

    @NotNull(message = "expense proof type id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_expense_proof_type_id", nullable = false)
    private ExpenseProofType fkExpenseProofType;


}