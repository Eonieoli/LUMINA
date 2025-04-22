package com.lumina.backend.user.service;

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
    Map<String, Object> getFollowers(Long targetUserId, boolean isMe, int pageNum);
}
