package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserProfileResponse {

    private Long userId;
    private String nickname;
    private String profileImage;
    private String message;
    private int positiveness;
    private int grade;
    private int rank;
    private int postCnt;
    private int followerCnt;
    private int followingCnt;
    private Boolean isFollowing;
}
