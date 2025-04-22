package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetFollowsResponse {

    private Long userId;
    private String profileImage;
    private String nickName;
    private Boolean isFollowing;
}
