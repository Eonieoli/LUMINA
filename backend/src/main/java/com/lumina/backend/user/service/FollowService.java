package com.lumina.backend.user.service;

import com.lumina.backend.user.model.response.GetFollowsResponse;

import java.util.List;
import java.util.Map;

/**
 * 팔로우(Follow) 관련 기능을 정의한 인터페이스입니다.
 * 구현체는 FollowServiceImpl 입니다.
 */
public interface FollowService {

    /**
     * 팔로우 관계를 토글(추가/삭제)하는 메서드
     */
    Boolean toggleFollow(Long followerId, Long followingId);

    /**
     * 유저의 팔로워 목록을 조회하는 메서드
     */
    List<GetFollowsResponse> getFollowers(Long myId, Long targetUserId);

    /**
     * 유저의 팔로잉 목록을 조회하는 메서드
     */
    List<GetFollowsResponse> getFollowings(Long myId, Long targetUserId);

    /**
     * 현재 사용자의 팔로워를 삭제하는 메서드
     */
    void deleteMyFollower(Long myId, Long userId);
}
