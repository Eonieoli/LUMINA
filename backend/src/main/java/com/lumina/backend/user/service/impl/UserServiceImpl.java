package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.service.S3Service;
import com.lumina.backend.common.utill.*;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.*;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    private final S3Service s3Service;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final TokenUtil tokenUtil;
    private final UserUtil userUtil;

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
     * @return GetMyProfileResponse 사용자 프로필 정보 응답
     */
    @Override
    public GetMyProfileResponse getMyProfile(Long userId) {

        User user = userUtil.getUserById(userId);

        int sumPointRank = getUserRankFromRedis(userId);
        int postCnt = postRepository.countByUserId(userId);
        int followerCnt = followRepository.countByFollowingId(userId);
        int followingCnt = followRepository.countByFollowerId(userId);

        return new GetMyProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(), user.getGrade(),
                sumPointRank, postCnt, followerCnt, followingCnt
        );
    }


    /**
     * 특정 사용자의 프로필 정보를 조회하는 메서드
     *
     * @param myId 현재 로그인한 사용자의 ID
     * @param userId 조회할 사용자의 ID
     * @return GetUserProfileResponse 사용자 프로필 정보 응답
     */
    @Override
    public GetUserProfileResponse getUserProfile(
            Long myId, Long userId) {

        ValidationUtil.validateUserId(userId);

        User user = userUtil.getUserById(userId);

        int sumPointRank = getUserRankFromRedis(userId);
        int postCnt = postRepository.countByUserId(userId);
        int followerCnt = followRepository.countByFollowingId(userId);
        int followingCnt = followRepository.countByFollowerId(userId);
        Boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myId, userId);

        return new GetUserProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(), user.getGrade(),
                sumPointRank, postCnt, followerCnt, followingCnt, isFollowing
        );
    }


    /**
     * 현재 사용자의 프로필 정보를 수정하는 메서드
     *
     * @param userId 수정할 사용자의 ID
     * @param request 수정할 프로필 정보
     * @param response HTTP 응답 객체
     */
    @Override
    @Transactional
    public void updateMyProfile(
            Long userId, HttpServletRequest httpRequest,
            UpdateMyProfileRequest request, HttpServletResponse response) throws IOException {

        ValidationUtil.validateRequiredField(request.getNickname(), "닉네임");
        ValidationUtil.validateRequiredField(request.getMessage(), "상태 메시지");

        User user = userUtil.getUserById(userId);
        validateDuplicateNickname(request.getNickname(), userId);
        String profileImageUrl = handleProfileImageUpdate(userId, request.getProfileImageFile());

        if (!user.getNickname().equals(request.getNickname())) {
            reissueTokens(userId, request.getNickname(), httpRequest, response);
        }

        user.updateProfile(profileImageUrl, request.getNickname(), request.getMessage());
        userRepository.save(user);
    }


    /**
     * 현재 사용자의 포인트 조회하는 메서드
     *
     * @param userId 수정할 사용자의 ID
     * @return GetUserPointResponse 유저 표인트 정보 응답
     */
    @Override
    public GetUserPointResponse getUserPoint(Long userId) {

        User user = userUtil.getUserById(userId);

        return new GetUserPointResponse(user.getId(), user.getNickname(), user.getPoint());
    }


    /**
     * 사용자 검색 기능을 제공하는 메서드
     *
     * @param keyword 검색할 닉네임 텍스트
     * @return Map<String, Object> 검색된 사용자 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> searchUser(String keyword, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findByNicknameContaining(keyword, pageRequest);

        List<SearchUserResponse> users = userPage.getContent().stream()
                .map(SearchUserResponse::from)
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(userPage, pageNum, "users", users);
    }


    /**
     * 현재 사용자와 10등까지 사용자들의 등수를 제공하는 메서드
     *
     * @param userId 현재 사용자의 ID
     * @return List<GetSumPointRankResponse> 사용자들의 등수 응답
     */
    @Override
    public List<GetSumPointRankResponse> getSumPointRank(Long userId) {

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        User my = userUtil.getUserById(userId);
        Long myRank = redisUtil.getUserRank(rankKey, userKey);

        List<String> userKeys = redisUtil.getTopRankersInOrder(rankKey, 0, 9);
        List<Long> userIds = userKeys.stream()
                .map(key -> Long.parseLong(key.replace("user:", "")))
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<GetSumPointRankResponse> rankList = new ArrayList<>();
        rankList.add(toRankResponse(my, myRank != null ? myRank.intValue() + 1 : -1));
        for (int i = 0; i < userIds.size(); i++) {
            User user = userMap.get(userIds.get(i));
            if (user != null)
                rankList.add(toRankResponse(user, i + 1));
        }
        return rankList;
    }


    private int getUserRankFromRedis(Long userId) {

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        Long rank = redisUtil.getUserRank(rankKey, userKey);
        return (rank != null) ? rank.intValue() + 1 : 0;
    }

    private void validateDuplicateNickname(
            String nickname, Long userId) {

        User existingUser = userRepository.findByNickname(nickname).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new CustomException(HttpStatus.CONFLICT, "닉네임 중복");
        }
    }

    private String handleProfileImageUpdate(
            Long userId, MultipartFile newFile) throws IOException {

        String existingImage = userRepository.findProfileImageByUserId(userId);
        if (newFile != null && !newFile.isEmpty()) {
            s3Service.deleteImageFile(existingImage, "profile/");
            return s3Service.uploadImageFile(newFile, "profile/");
        }
        return existingImage;
    }

    private void reissueTokens(
            Long userId, String newNickname,
            HttpServletRequest request, HttpServletResponse response) {

        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String deviceType = redisUtil.getDeviceType(userAgent);
        String userKey = "refresh:" + userId + ":" + deviceType;
        String role = tokenUtil.findRoleByToken(request);

        String newAccess = jwtUtil.createJwt("access", newNickname, role, Long.parseLong(jwtAccessExp));
        String newRefresh = jwtUtil.createJwt("refresh", newNickname, role, Long.parseLong(jwtRefreshExp));

        redisUtil.setex(userKey, newRefresh, Long.parseLong(jwtRedisExp));

        response.addCookie(CookieUtil.createCookie("access", newAccess));
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh));
    }

    private GetSumPointRankResponse toRankResponse(User user, int rank) {
        return new GetSumPointRankResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getSumPoint(),
                rank
        );
    }
}
