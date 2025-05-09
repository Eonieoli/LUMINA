package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
public class GetMyReward {

    private Long postId;
    private Long commentId;
    private String content;
    private Integer point;
    private Integer positiveness;
    private LocalDateTime createdAt;
}
