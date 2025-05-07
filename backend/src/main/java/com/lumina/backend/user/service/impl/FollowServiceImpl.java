package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.PagingResponseUtil;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.user.model.entity.Follow;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.response.GetFollowsResponse;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 팔로우 관련 서비스를 제공하는 클래스
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    private final FindUtil findUtil;


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

        ValidationUtil.validateId(followerId, "사용자");
        ValidationUtil.validateId(followingId, "사용자");
        ValidationUtil.validateFollow(followerId, followingId);

        User follower = findUtil.getUserById(followerId);
        User following = findUtil.getUserById(followingId);

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
     * 사용자의 팔로워 목록을 조회하는 메서드
     *
     * @param targetUserId 유저의 ID
     * @return Map<String, Object> 팔로워 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> getFollowers(
            Long myId, Long targetUserId, int pageNum) {

        return getFollows(myId, targetUserId, pageNum,
                followRepository::findByFollowingId, "followers");
    }


    /**
     * 사용자의 팔로잉 목록을 조회하는 메서드
     *
     * @param targetUserId 유저의 ID
     * @return Map<String, Object> 팔로잉 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> getFollowings(
            Long myId, Long targetUserId, int pageNum) {

        return getFollows(myId, targetUserId, pageNum,
                followRepository::findByFollowerId, "followings");
    }


    /**
     * 현재 사용자의 팔로워를 삭제하는 메서드
     *
     * @param myId 현재 로그인한 사용자의 ID
     * @param userId 삭제할 팔로워의 ID
     */
    @Override
    public void deleteMyFollower(
            Long myId, Long userId) {

        ValidationUtil.validateId(userId, "사용자");

        Follow follow = followRepository.findByFollowerIdAndFollowingId(userId, myId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "팔로워 관계를 찾을 수 없음"));

        followRepository.delete(follow);
    }


    private List<GetFollowsResponse> convertToFollowResponses(
            List<Follow> follows, Long myId,
            Function<Follow, User> userExtractor) {

        return follows.stream()
                .map(follow -> {
                    User user = userExtractor.apply(follow);
                    boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myId, user.getId());
                    return new GetFollowsResponse(
                            user.getId(),
                            user.getProfileImage(),
                            user.getNickname(),
                            isFollowing
                    );
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> getFollows(
            Long myId, Long targetUserId, int pageNum,
            BiFunction<Long, Pageable, Page<Follow>> followFetchFunction, String keyName) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 20);
        Page<Follow> followPage = followFetchFunction.apply(targetUserId, pageRequest);

        List<GetFollowsResponse> responses = convertToFollowResponses(
                followPage.getContent(), myId,
                keyName.equals("followers") ? Follow::getFollower : Follow::getFollowing);

        return PagingResponseUtil.toPagingResult(followPage, pageNum, keyName, responses);
    }
}
