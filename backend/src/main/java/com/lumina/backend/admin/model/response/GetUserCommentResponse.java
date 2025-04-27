package com.lumina.backend.admin.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserCommentResponse {

    private Long commentId;
    private Long postId;
    private String commentContent;
}
