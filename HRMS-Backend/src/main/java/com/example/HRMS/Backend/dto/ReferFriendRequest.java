package com.example.HRMS.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReferFriendRequest {

    @Size(max = 50)
    @NotNull(message = "refer friend name is required")
    private String referFriendName;

    @Size(max = 50)
    @Email(message = "email formate is not proper")
    @NotNull(message = "refer friend email is required")
    private String referFriendEmail;

    @Size(max = 255)
    private String referFriendShortNote;

    @NotNull(message = "cv status type is required")
    private Long fkCvStatusTypeId;

    private String reasonForCvStatusChange;

    @NotNull(message = "who refer this?(employee id) is required")
    private Long fkReferFriendEmployeeId;

    @NotNull(message = "job id is required")
    private Long fkJobId;
}
