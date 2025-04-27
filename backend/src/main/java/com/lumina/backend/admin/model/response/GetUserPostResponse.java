package com.lumina.backend.admin.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserPostResponse {

    private Long postId;
    private String postImage;
    private String postContent;
}
