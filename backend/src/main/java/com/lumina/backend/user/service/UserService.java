package com.lumina.backend.user.service;

import com.lumina.backend.user.model.response.GetMyProfileResponse;

public interface UserService {

    /**
     * 내 프로필 정보를 반환합니다.
     */
    GetMyProfileResponse getMyProfile(Long userId);
}
