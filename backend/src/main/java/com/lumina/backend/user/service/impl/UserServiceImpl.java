package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.response.GetMyProfileResponse;
import com.lumina.backend.user.model.response.GetUserProfileResponse;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;


    /**
     * 현재 사용자의 프로필 정보를 조회하는 메서드
     *
     * @param userId 조회할 사용자의 ID
     * @return ResponseEntity<BaseResponse<GetMyProfileResponse>> 사용자 프로필 정보 응답
     * @throws CustomException 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    public GetMyProfileResponse getMyProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        int followerCnt = followRepository.countByFollowingId(userId);
        int followingCnt = followRepository.countByFollowerId(userId);

        GetMyProfileResponse response = new GetMyProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(), user.getMessage(),
                user.getPositiveness(), user.getGrade(), followerCnt, followingCnt
        );

        return response;
    }


    /**
     * 특정 사용자의 프로필 정보를 조회하는 메서드
     *
     * @param myId 현재 로그인한 사용자의 ID
     * @param userId 조회할 사용자의 ID
     * @return ResponseEntity<BaseResponse<GetUserProfileResponse>> 사용자 프로필 정보 응답
     * @throws CustomException 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    public GetUserProfileResponse getUserProfile(
            Long myId, Long userId) {

        if (userId == null || userId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        int followerCnt = followRepository.countByFollowingId(userId);
        int followingCnt = followRepository.countByFollowerId(userId);

        Boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myId, userId);

        GetUserProfileResponse response = new GetUserProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(), user.getGrade(),
                followerCnt, followingCnt, isFollowing
        );

        return response;
    }
}
