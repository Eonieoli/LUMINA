package com.lumina.backend.post.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadCommentRequest {

    private String commentContent;
    private Long parentCommentId;
}
