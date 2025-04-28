package com.lumina.backend.post.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetPostResponse {

    private Long postId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String postImage;
    private String postContent;
    private int postViews;
    private String categoryName;
    private List<String> hashtagList;
    private int likeCnt;
    private int commentCnt;
    private Boolean isLike;
}
