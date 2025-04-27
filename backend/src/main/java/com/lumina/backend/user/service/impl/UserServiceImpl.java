package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.post.service.S3Service;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.request.DoDonationRequest;
import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.*;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final DonationRepository donationRepository;

    private final OAuthService oAuthService;
    private final S3Service s3Service;

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
        if (request.getProfileImageFile() != null && !request.getProfileImageFile().isEmpty()) {
            s3Service.deleteImageFile(existingProfileImageUrl, "profile/"); // 기존 이미지 삭제
            profileImageUrl = s3Service.uploadImageFile(request.getProfileImageFile(), "profile/");
        }

        // 닉네임 변경 시 토큰 재발급
        if (!user.getNickname().equals(request.getNickname())) {
            // 기기 정보 가져오기
            String userAgent = httpRequest.getHeader("User-Agent").toLowerCase();
            String deviceType = oAuthService.getDeviceType(userAgent); // 기기 유형 판별
            String userKey = "refresh:" + userId + ":" + deviceType;
            String role = oAuthService.findRoleByToken(httpRequest);

            // 새로운 액세스 및 리프레시 토큰 생성
            String newAccess = jwtUtil.createJwt("access", request.getNickname(), role, Long.parseLong(jwtAccessExp)); // 10분 유효
            String newRefresh = jwtUtil.createJwt("refresh", request.getNickname(), role, Long.parseLong(jwtRefreshExp)); // 1일 유효

            // Redis에 새 리프레시 토큰 저장
            redisUtil.setex(userKey, newRefresh, Long.parseLong(jwtRedisExp));

            // 클라이언트에 새 토큰 쿠키로 설정
            response.addCookie(oAuthService.createCookie("access", newAccess));
            response.addCookie(oAuthService.createCookie("refresh", newRefresh));
        }

        user.updateProfile(profileImageUrl, request.getNickname(), request.getMessage());
        userRepository.save(user);
    }


    @Override
    public GetUserPointResponse getUserPoint(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        return new GetUserPointResponse(user.getId(), user.getNickname(), user.getPoint());
    }


    @Override
    @Transactional
    public void doDonation(
            Long userId, DoDonationRequest request) {

        if (request.getDonationName() == null || request.getDonationName().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "기부처는 필수 입력값입니다.");
        }

        if (request.getPoint() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "point는 필수 입력값입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        Donation donation = donationRepository.findByDonationName(request.getDonationName())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "기부처를 찾을 수 없음: " + request.getDonationName()));

        if (user.getPoint() < request.getPoint()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "보유 point가 부족합니다.");
        }

        user.updatePoint(-request.getPoint());
        user.updateSumPoint(request.getPoint());
        user.updatePositiveness(request.getPoint() / 100);
        User savedUser = userRepository.save(user);

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;

        redisUtil.addSumPointToZSetWithTTL(rankKey, userKey, savedUser.getSumPoint());
    }


    /**
     * 사용자 검색 기능을 제공하는 메서드
     *
     * @param keyword 검색할 닉네임 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색된 사용자 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> searchUser(
            String keyword, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findByNicknameContaining(keyword, pageRequest);

        // 조회된 사용자 목록을 SearchUsersResponse DTO로 변환
        List<SearchUserResponse> users = userPage.getContent().stream()
                        .map(user -> {
                            return new SearchUserResponse(
                                    user.getId(),
                                    user.getProfileImage(),
                                    user.getNickname()
                            );
                        })
                        .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", userPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("users", users);

        // 3. 성공 응답 생성 및 반환
        return result;
    }


    @Override
    public List<GetSumPointRankResponse> getSumPointRank(
            Long userId) {

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        List<GetSumPointRankResponse> rankList = new ArrayList<>();

        // 내 정보 세팅
        User my = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));
        Long myRank = redisUtil.getUserRank(rankKey, userKey);

        rankList.add(new GetSumPointRankResponse(
                my.getId(),
                my.getNickname(),
                my.getProfileImage(),
                my.getSumPoint(),
                myRank.intValue() + 1
        ));

        // 10등까지의 랭킹 정보 가져오기
        List<String> userKeys = redisUtil.getTopRankersInOrder(rankKey, 0, 9);

        // 랭킹에 포함된 유저 정보 조회 (userId는 Long이므로 변환)
        List<Long> userIds = userKeys.stream()
                .map(key -> Long.parseLong(key.replace("user:", "")))
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 3. 10등까지의 정보 세팅
        for (int i = 0; i < userIds.size(); i++) {
            Long uid = userIds.get(i);
            User user = userMap.get(uid);
            if (user == null) continue; // 혹시라도 유저가 없는 경우 방어

            rankList.add(new GetSumPointRankResponse(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileImage(),
                    user.getPositiveness(),
                    i + 1 // 1등부터 시작
            ));
        }

        return rankList;
    }
}
