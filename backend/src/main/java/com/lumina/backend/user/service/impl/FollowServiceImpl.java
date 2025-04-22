package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.user.model.entity.Follow;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 팔로우 관련 서비스를 제공하는 클래스
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;


    /**
     * 팔로우 관계를 토글(추가/삭제)하는 메서드
     *
     * @param followerId 팔로우를 하는 사용자의 ID
     * @param followingId 팔로우 대상 사용자의 ID
     * @return Boolean 팔로우 관계가 생성되면 true, 삭제되면 false
     */
    @Override
    @Transactional
    public Boolean toggleFollow(
            Long followerId, Long followingId) {

        if (followerId == null || followerId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 팔로워 ID입니다.");
        }

        if (followingId == null || followingId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 팔로잉 ID입니다.");
        }

        // 팔로워와 팔로잉 ID가 같은 경우 예외 처리
        if (followerId.equals(followingId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "자신을 팔로우할 수 없습니다.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팔로워 사용자를 찾을 수 없습니다. 사용자 ID: " + followerId));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팔로잉 사용자를 찾을 수 없습니다. 사용자 ID: " + followingId));

        // 기존 팔로우 관계 확인
        Follow existingFollow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElse(null);

        if (existingFollow != null) {
            // 기존 팔로우 관계가 있으면 삭제
            followRepository.delete(existingFollow);
            return false;
        } else {
            // 팔로우 관계가 없으면 새로 생성
            Follow follow = new Follow(follower, following);
            followRepository.save(follow);
            return true;
        }
    }
}
