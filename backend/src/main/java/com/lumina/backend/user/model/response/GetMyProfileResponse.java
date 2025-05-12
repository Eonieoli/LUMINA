package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetMyProfileResponse {

    private Long userId;
    private String nickname;
    private String profileImage;
    private String message;
    private int positiveness;
    private int rank;
    private int postCnt;
    private int followerCnt;
    private int followingCnt;
}
