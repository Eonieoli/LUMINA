package com.lumina.backend.post.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetChildCommentResponse {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String commentContent;
    private int likeCnt;
    private Boolean isLike;
}
