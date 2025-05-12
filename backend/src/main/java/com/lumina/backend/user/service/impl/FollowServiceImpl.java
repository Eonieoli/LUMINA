package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.user.model.entity.Follow;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.response.GetFollowsResponse;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    private final FindUtil findUtil;


    /**
     * 팔로우 관계 생성/삭제 토글 기능 수행
     *
     * @param followerId 팔로우 요청 사용자 ID
     * @param followingId 대상 사용자 ID
     * @return Boolean 생성 시 true, 삭제 시 false
     * @throws CustomException 유효성 검증 실패 시 발생
     */
    @Override
    @Transactional
    public Boolean toggleFollow(
            Long followerId, Long followingId) {

        ValidationUtil.validateId(followerId, "사용자");
        ValidationUtil.validateId(followingId, "사용자");
        // 자기 자신 팔로우 방지
        ValidationUtil.validateFollow(followerId, followingId);

        User follower = findUtil.getUserById(followerId);
        User following = findUtil.getUserById(followingId);

        // 존재하는 관계인지 확인 후 삭제/생성 분기 처리
        return followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .map(existingFollow -> {
                    followRepository.delete(existingFollow);
                    return false;
                })
                .orElseGet(() -> {
                    followRepository.save(new Follow(follower, following));
                    return true;
                });
    }


    /**
     * 특정 사용자의 팔로워 목록 조회 (현재 사용자의 팔로우 상태 포함)
     *
     * @param myId 현재 로그인 사용자 ID (팔로우 상태 확인용)
     * @param targetUserId 조회 대상 사용자 ID
     * @return List<GetFollowsResponse> 팔로워 정보 + 현재 사용자의 팔로우 상태
     */
    @Override
    public List<GetFollowsResponse> getFollowers(
            Long myId, Long targetUserId) {

        List<Follow> followList = followRepository.findByFollowingId(targetUserId);

        return followList.stream()
                .map(follow -> convertToFollowResponses(myId, follow.getFollower()))
                .collect(Collectors.toList());
    }


    /**
     * 특정 사용자의 팔로잉 목록 조회 (현재 사용자의 팔로우 상태 포함)
     *
     * @param myId 현재 로그인 사용자 ID
     * @param targetUserId 조회 대상 사용자 ID
     * @return List<GetFollowsResponse> 팔로잉 정보 + 현재 사용자의 팔로우 상태
     */
    @Override
    public List<GetFollowsResponse> getFollowings(
            Long myId, Long targetUserId) {

        List<Follow> followList = followRepository.findByFollowerId(targetUserId);

        return followList.stream()
                .map(follow -> convertToFollowResponses(myId, follow.getFollowing()))
                .collect(Collectors.toList());
    }


    /**
     * 현재 사용자의 특정 팔로워 삭제 (차단 기능)
     *
     * @param myId 현재 로그인 사용자 ID
     * @param userId 삭제할 팔로워 사용자 ID
     * @throws CustomException 팔로워 관계 없을 경우 404 에러
     */
    @Override
    public void deleteMyFollower(
            Long myId, Long userId) {

        ValidationUtil.validateId(userId, "사용자");

        Follow follow = followRepository.findByFollowerIdAndFollowingId(userId, myId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팔로워 관계를 찾을 수 없음"));

        followRepository.delete(follow);
    }


    /**
     * 사용자 정보 → 응답 DTO 변환 (현재 사용자의 팔로우 상태 포함)
     *
     * @param myId 현재 사용자 ID
     * @param follow 대상 사용자 엔티티
     * @return GetFollowsResponse 변환된 응답 객체
     */
    private GetFollowsResponse convertToFollowResponses(Long myId, User follow) {
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myId, follow.getId());

        return new GetFollowsResponse(
                follow.getId(),
                follow.getProfileImage(),
                follow.getNickname(),
                isFollowing
        );
    }
}
