package com.example.HRMS.Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {

    @NotNull(message = "post id require")
    private Long fkPostId;

    @NotNull(message = "employee id require woh make like")
    private Long fkLikeEmployeeId;

    private Long fkCommentId;

}
