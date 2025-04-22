package com.lumina.backend.user.service;

import com.lumina.backend.user.model.response.GetMyProfileResponse;
import com.lumina.backend.user.model.response.GetUserProfileResponse;

public interface UserService {

    /**
     * 내 프로필 정보를 반환합니다.
     */
    GetMyProfileResponse getMyProfile(Long userId);

    /**
     * 다른 사용자의 프로필 정보를 반환합니다.
     */
    GetUserProfileResponse getUserProfile(Long myId, Long userId);
}
