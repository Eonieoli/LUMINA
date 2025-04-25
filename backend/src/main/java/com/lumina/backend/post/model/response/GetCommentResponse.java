package com.lumina.backend.post.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCommentResponse {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String commentContent;
    private int likeCnt;
    private int childCommentCnt;
    private Boolean isLike;
}
