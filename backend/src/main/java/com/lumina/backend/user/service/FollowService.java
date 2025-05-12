package com.lumina.backend.user.service;

import com.lumina.backend.user.model.response.GetFollowsResponse;

import java.util.List;

public interface FollowService {

    Boolean toggleFollow(Long followerId, Long followingId);

    List<GetFollowsResponse> getFollowers(Long myId, Long targetUserId);

    List<GetFollowsResponse> getFollowings(Long myId, Long targetUserId);

    void deleteMyFollower(Long myId, Long userId);
}
