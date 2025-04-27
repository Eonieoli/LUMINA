package com.lumina.backend.user.service;

import com.lumina.backend.user.model.request.DoDonationRequest;
import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.GetMyProfileResponse;
import com.lumina.backend.user.model.response.GetUserPointResponse;
import com.lumina.backend.user.model.response.GetUserProfileResponse;
import com.lumina.backend.user.model.response.SearchUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {

    /**
     * 내 프로필 정보를 반환합니다.
     */
    GetMyProfileResponse getMyProfile(Long userId);

    /**
     * 다른 사용자의 프로필 정보를 반환합니다.
     */
    GetUserProfileResponse getUserProfile(Long myId, Long userId);

    /**
     * 내 프로필을 수정합니다.
     */
    void updateMyProfile(Long userId, HttpServletRequest request, UpdateMyProfileRequest updateRequest, HttpServletResponse response) throws IOException;

    GetUserPointResponse getUserPoint(Long userId);

    void doDonation(Long userId, DoDonationRequest request);

    Map<String, Object> searchUser(String keyword, int pageNum);
}
