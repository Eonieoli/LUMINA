package com.lumina.backend.user.service;

import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {

    GetMyProfileResponse getMyProfile(Long userId);

    GetUserProfileResponse getUserProfile(Long myId, Long userId);

    void updateMyProfile(Long userId, HttpServletRequest request, UpdateMyProfileRequest updateRequest, HttpServletResponse response) throws IOException;

    GetUserPointResponse getUserPoint(Long userId);

    Map<String, Object> searchUser(String keyword, int pageNum);

    Map<String, Object> getSumPointRank(Long userId);

    List<GetMyReward> getMyReward(Long userId);
}
