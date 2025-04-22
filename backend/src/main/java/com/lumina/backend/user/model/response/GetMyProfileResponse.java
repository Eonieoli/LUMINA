package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetMyProfileResponse {

    private Long userId;
    private String nickname;
    private String profileImage;
    private String message;
    private int positiveness;
    private int grade;
    private int followerCnt;
    private int followingCnt;
}
