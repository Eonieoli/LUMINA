package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.GetMyProfileResponse;
import com.lumina.backend.user.model.response.GetUserProfileResponse;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    private final OAuthService oAuthService;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Value("${JWT_ACCESS_EXP}")
    private String jwtAccessExp;

    @Value("${JWT_REFRESH_EXP}")
    private String jwtRefreshExp;

    @Value("${JWT_REDIS_EXP}")
    private String jwtRedisExp;


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

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        Long rank = redisUtil.getUserRank(rankKey, userKey);
        int sumPointRank = (rank != null) ? rank.intValue() + 1 : 0;

        int postCnt = postRepository.countByUserId(userId);

        GetMyProfileResponse response = new GetMyProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(), user.getGrade(),
                sumPointRank, postCnt, followerCnt, followingCnt
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

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        Long rank = redisUtil.getUserRank(rankKey, userKey);
        int sumPointRank = (rank != null) ? rank.intValue() + 1 : 0;

        int postCnt = postRepository.countByUserId(userId);

        Boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myId, userId);

        GetUserProfileResponse response = new GetUserProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(), user.getGrade(),
                sumPointRank, postCnt, followerCnt, followingCnt, isFollowing
        );

        return response;
    }


    /**
     * 현재 사용자의 프로필 정보를 수정하는 메서드
     *
     * @param userId 수정할 사용자의 ID
     * @param request 수정할 프로필 정보
     * @param response HTTP 응답 객체
     * @return ResponseEntity<BaseResponse<Void>> 수정 결과 응답
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    @Transactional
    public void updateMyProfile(
            Long userId, HttpServletRequest httpRequest,
            UpdateMyProfileRequest request, HttpServletResponse response) throws IOException {

        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "닉네임은 필수 입력값입니다.");
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "상태 메시지는 필수 입력값입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        User existingUser = userRepository.findByNickname(request.getNickname())
                .orElse(null);

        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new CustomException(HttpStatus.CONFLICT, "닉네임 중복");
        }

        String existingProfileImageUrl = userRepository.findProfileImageByUserId(userId);
        String profileImageUrl = existingProfileImageUrl; // 기본적으로 기존 이미지 유지

        // 새 프로필 이미지가 들어왔을 때만 업데이트
//        if (request.getProfileImageFile() != null && !request.getProfileImageFile().isEmpty()) {
//            photoService.deleteProfileFile(existingProfileImageUrl); // 기존 이미지 삭제
//            profileImageUrl = photoService.uploadProfileFile(request.getProfileImageFile());
//        }

        // 닉네임 변경 시 토큰 재발급
        if (!user.getNickname().equals(request.getNickname())) {
            // 기기 정보 가져오기
            String userAgent = httpRequest.getHeader("User-Agent").toLowerCase();
            String deviceType = oAuthService.getDeviceType(userAgent); // 기기 유형 판별
            String userKey = "refresh:" + userId + ":" + deviceType;

            // 새로운 액세스 및 리프레시 토큰 생성
            String newAccess = jwtUtil.createJwt("access", request.getNickname(), Long.parseLong(jwtAccessExp)); // 10분 유효
            String newRefresh = jwtUtil.createJwt("refresh", request.getNickname(), Long.parseLong(jwtRefreshExp)); // 1일 유효

            // Redis에 새 리프레시 토큰 저장
            redisUtil.setex(userKey, newRefresh, Long.parseLong(jwtRedisExp));

            // 클라이언트에 새 토큰 쿠키로 설정
            response.addCookie(oAuthService.createCookie("access", newAccess));
            response.addCookie(oAuthService.createCookie("refresh", newRefresh));
        }

        user.updateProfile(profileImageUrl, request.getNickname(), request.getMessage());
        userRepository.save(user);
    }
}
