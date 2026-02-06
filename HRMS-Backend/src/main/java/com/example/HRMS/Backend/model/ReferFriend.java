package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = "refer_friend")
public class ReferFriend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_refer_friend_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull(message = "refer friend name is required")
    @Column(name = "refer_friend_name", nullable = false, length = 50)
    private String referFriendName;

    @Size(max = 50)
    @Email(message = "email formate is not proper")
    @NotNull(message = "refer friend email is required")
    @Column(name = "refer_friend_email", nullable = false, length = 50)
    private String referFriendEmail;

    @Size(max = 255)
    @NotNull(message = "refer friend cv is required")
    @Column(name = "refer_friend_cv_url", nullable = false)
    private String referFriendCvUrl;

    @Size(max = 255)
    @Column(name = "refer_friend_short_note")
    private String referFriendShortNote;

    @ColumnDefault("getdate()")
    @Column(name = "refer_friend_review_status_changed_at")
    private Instant referFriendReviewStatusChangedAt;

    @NotNull(message = "cv status type is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_cv_status_type_id", nullable = false)
    private CvStatusType fkCvStatusType;

    @NotNull(message = "who refer this?(employee id) is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_refer_friend_employee_id", nullable = false)
    private Employee fkReferFriendEmployee;

    @NotNull(message = "job id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_job_id", nullable = false)
    private Job fkJob;


}