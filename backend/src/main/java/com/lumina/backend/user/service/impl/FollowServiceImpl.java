package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.user.model.entity.Follow;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.response.GetFollowsResponse;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    /**
     * 현재 사용자의 팔로워 목록을 조회하는 메서드
     *
     * @param targetUserId 유저의 ID
     * @return Map<String, Object> 팔로워 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> getFollowers(
            Long myId, Long targetUserId, boolean isMe, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 20);
        Page<Follow> followPage = followRepository.findByFollowingId(targetUserId, pageRequest);

        // 페이지 데이터 존재 여부 확인
        if (pageNum > followPage.getTotalPages() || followPage.getContent().isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "해당 페이지에 팔로워 정보가 없습니다.");
        }

        List<GetFollowsResponse> followerResponses = followPage.getContent().stream()
                .map(follow -> {
                    User follower = follow.getFollower();
                    boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(
                            myId, follower.getId());
                    return new GetFollowsResponse(
                            follower.getId(),
                            follower.getProfileImage(),
                            follower.getNickname(),
                            isFollowing
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("totalPages", followPage.getTotalPages());
        responseData.put("currentPage", pageNum);
        responseData.put("followers", followerResponses);

        return responseData;
    }


    /**
     * 현재 사용자의 팔로잉 목록을 조회하는 메서드
     *
     * @param targetUserId 유저의 ID
     * @return Map<String, Object> 팔로잉 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> getFollowings(
            Long myId, Long targetUserId, boolean isMe, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 20);
        Page<Follow> followPage = followRepository.findByFollowerId(targetUserId, pageRequest);

        // 페이지 데이터 존재 여부 확인
        if (pageNum > followPage.getTotalPages() || followPage.getContent().isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "해당 페이지에 팔로잉 정보가 없습니다.");
        }

        List<GetFollowsResponse> followingResponses = followPage.getContent().stream()
                .map(follow -> {
                    User following = follow.getFollowing();
                    boolean isFollowing = true;
                    if (!isMe) {
                        isFollowing = followRepository.existsByFollowerIdAndFollowingId(
                                myId, following.getId());
                    }
                    return new GetFollowsResponse(
                            following.getId(),
                            following.getProfileImage(),
                            following.getNickname(),
                            isFollowing
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("totalPages", followPage.getTotalPages());
        responseData.put("currentPage", pageNum);
        responseData.put("followings", followingResponses);

        return responseData;
    }

}
