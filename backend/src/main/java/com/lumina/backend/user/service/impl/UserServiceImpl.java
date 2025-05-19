package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.service.S3Service;
import com.lumina.backend.common.service.TokenService;
import com.lumina.backend.common.utill.*;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.model.entity.UserDonation;
import com.lumina.backend.donation.repository.UserDonationRepository;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.repository.CommentRepository;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserDonationRepository userDonationRepository;

    private final RedisUtil redisUtil;
    private final FindUtil findUtil;
    private final TokenUtil tokenUtil;

    private final S3Service s3Service;
    private final TokenService tokenService;

    @Value("${JWT_ACCESS_EXP}")
    private String jwtAccessExp;

    @Value("${JWT_REFRESH_EXP}")
    private String jwtRefreshExp;

    @Value("${JWT_REDIS_EXP}")
    private String jwtRedisExp;


    /**
     * 현재 사용자의 프로필 정보를 조회
     *
     * @param userId 조회할 사용자 ID
     * @return GetMyProfileResponse 사용자 프로필 정보 응답
     */
    @Override
    public GetMyProfileResponse getMyProfile(Long userId) {

        User user = findUtil.getUserById(userId);

        int sumPointRank = getUserRankFromRedis(userId); // Redis 에서 랭킹 조회
        int postCnt = postRepository.countByUserId(userId);
        int followerCnt = followRepository.countByFollowingId(userId);
        int followingCnt = followRepository.countByFollowerId(userId);

        return new GetMyProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(),
                sumPointRank, postCnt, followerCnt, followingCnt
        );
    }


    /**
     * 특정 사용자의 프로필 정보를 조회
     *
     * @param myId 현재 로그인한 사용자 ID
     * @param userId 조회할 사용자 ID
     * @return GetUserProfileResponse 사용자 프로필 정보 응답
     */
    @Override
    public GetUserProfileResponse getUserProfile(
            Long myId, Long userId) {

        ValidationUtil.validateId(userId, "사용자");

        User user = findUtil.getUserById(userId);

        int sumPointRank = getUserRankFromRedis(userId);
        int postCnt = postRepository.countByUserId(userId);
        int followerCnt = followRepository.countByFollowingId(userId);
        int followingCnt = followRepository.countByFollowerId(userId);
        Boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myId, userId);

        return new GetUserProfileResponse(
                user.getId(), user.getNickname(), user.getProfileImage(),
                user.getMessage(), user.getPositiveness(),
                sumPointRank, postCnt, followerCnt, followingCnt, isFollowing
        );
    }


    /**
     * 현재 사용자의 프로필 정보 수정
     *
     * @param userId 수정할 사용자 ID
     * @param httpRequest HTTP 요청 객체
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

        User user = findUtil.getUserById(userId);
        validateDuplicateNickname(request.getNickname(), userId); // 닉네임 중복 체크
        String profileImageUrl = handleProfileImageUpdate(
                userId, request.getProfileImageFile(), request.getDefaultImage()); // 프로필 이미지 변경 처리

        // 닉네임 변경 시 토큰 재발급
        if (!user.getNickname().equals(request.getNickname())) {
            String userKey = redisUtil.getRefreshKey(httpRequest, userId);
            String role = tokenUtil.findRoleByToken(httpRequest);
            tokenService.reissueTokens(userKey, request.getNickname(), role, response);
        }

        user.updateProfile(profileImageUrl, request.getNickname(), request.getMessage());
        userRepository.save(user);
    }


    /**
     * 현재 사용자의 포인트 조회
     *
     * @param userId 사용자 ID
     * @return GetUserPointResponse 유저 포인트 정보 응답
     */
    @Override
    public GetUserPointResponse getUserPoint(Long userId) {

        User user = findUtil.getUserById(userId);

        return new GetUserPointResponse(user.getId(), user.getNickname(), user.getPoint());
    }


    /**
     * 사용자 검색 기능 제공
     *
     * @param keyword 검색할 닉네임 텍스트
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 검색 결과 및 페이징 정보
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
     * 현재 사용자와 10등까지 사용자들의 등수 조회
     *
     * @param userId 현재 사용자 ID
     * @return Map<String, Object> 등수 정보 응답
     */
    @Override
    public Map<String, Object> getSumPointRank(Long userId) {

        String rankKey = "sum-point:rank";
        User my = findUtil.getUserById(userId);
        Long myRank = redisUtil.getUserRank(rankKey, "user:" + userId);

        // 상위 10명 사용자 키 조회
        List<String> userKeys = redisUtil.getTopRankersInOrder(rankKey, 0, 9);
        List<Long> userIds = userKeys.stream()
                .map(key -> Long.parseLong(key.replace("user:", "")))
                .toList();

        // ID로 사용자 정보 일괄 조회
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 현재 사용자와 상위 10명 랭킹 응답
        List<GetSumPointRankResponse> rankList = new ArrayList<>();
        rankList.add(toRankResponse(my, myRank != null ? myRank.intValue() + 1 : -1));
        for (int i = 0; i < userIds.size(); i++) {
            User user = userMap.get(userIds.get(i));
            if (user != null)
                rankList.add(toRankResponse(user, i + 1));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalUser", userRepository.countByUserStatusTrue());
        result.put("ranks", rankList);
        return result;
    }


    /**
     * 사용자의 보상 내역 조회 (게시글/댓글)
     *
     * @param userId 사용자 ID
     * @return List<GetMyReward> 보상 내역 리스트
     */
    @Override
    public List<GetMyRewardRespond> getMyReward(Long userId) {

        List<GetMyRewardRespond> postRewards = postRepository.findByUserId(userId).stream()
                .map(post -> toRewardDto(post, null))
                .toList();

        List<GetMyRewardRespond> commentRewards = commentRepository.findByUserId(userId).stream()
                .map(comment -> toRewardDto(comment.getPost(), comment))
                .toList();

        return Stream.concat(postRewards.stream(), commentRewards.stream())
                .sorted(Comparator.comparing(GetMyRewardRespond::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }


    /**
     * 주어진 사용자 ID에 해당하는 기부 내역 리스트를 조회하여 응답 DTO 리스트로 변환합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 기부 내역을 담은 GetUserDonation DTO 리스트
     */
    @Override
    public List<GetUserDonation> getUserDonation(Long userId) {

        List<UserDonation> userDonationList = userDonationRepository.findByUserIdAndRegistration(userId, "DONATION");

        return userDonationList.stream()
                .map(this::convertToGetUserDonationResponses)
                .collect(Collectors.toList());
    }


    /**
     * Redis에서 사용자 랭킹 조회
     *
     * @param userId 사용자 ID
     * @return int 랭킹 (없으면 0)
     */
    private int getUserRankFromRedis(Long userId) {

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        Long rank = redisUtil.getUserRank(rankKey, userKey);
        return (rank != null) ? rank.intValue() + 1 : 0;
    }

    /**
     * 닉네임 중복 검증
     *
     * @param nickname 검증할 닉네임
     * @param userId 현재 사용자 ID
     * @throws CustomException 중복 시 예외 발생
     */
    private void validateDuplicateNickname(
            String nickname, Long userId) {

        User existingUser = userRepository.findByNickname(nickname).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new CustomException(HttpStatus.CONFLICT, "닉네임 중복");
        }
    }

    /**
     * 프로필 이미지 변경 처리
     *
     * @param userId 사용자 ID
     * @param newFile 새 이미지 파일
     * @param defaultImage 기본 이미지로 변경 여부
     * @return String 변경된 이미지 URL (기본 이미지면 null)
     * @throws IOException 파일 처리 예외
     */
    private String handleProfileImageUpdate(
            Long userId, MultipartFile newFile,
            Boolean defaultImage) throws IOException {

        String existingImage = userRepository.findProfileImageByUserId(userId);

        if (defaultImage) {
            if (existingImage != null) {
                s3Service.deleteImageFile(existingImage, "profile/");
            }
            return null;
        }

        if (newFile != null && !newFile.isEmpty()) {
            s3Service.deleteImageFile(existingImage, "profile/");
            return s3Service.uploadImageFile(newFile, "profile/");
        }
        return existingImage;
    }

    /**
     * 사용자 및 랭킹 정보를 응답 DTO로 변환합니다.
     *
     * @param user 사용자 엔티티
     * @param rank 해당 사용자의 랭킹 (1부터 시작)
     * @return GetSumPointRankResponse 랭킹 응답 DTO
     */
    private GetSumPointRankResponse toRankResponse(User user, int rank) {
        return new GetSumPointRankResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getSumPoint(),
                rank
        );
    }

    /**
     * 게시글 또는 댓글 보상 정보를 응답 DTO로 변환합니다.
     *
     * @param post 게시글 엔티티 (항상 필수)
     * @param comment 댓글 엔티티 (게시글 보상 시 null)
     * @return GetMyReward 보상 응답 DTO
     */
    private GetMyRewardRespond toRewardDto(Post post, Comment comment) {

        // Post 보상
        if (comment == null) {
            int reward = post.getPostReward();
            return GetMyRewardRespond.builder()
                    .postId(post.getId())
                    .content(post.getPostContent())
                    .point(reward >= 0 ? reward : null)
                    .positiveness(reward < 0 ? reward : null)
                    .createdAt(post.getCreatedAt())
                    .build();
        }
        // Comment 보상
        int reward = comment.getCommentReward();
        return GetMyRewardRespond.builder()
                .postId(post.getId())
                .commentId(comment.getId())
                .content(comment.getCommentContent())
                .point(reward >= 0 ? reward : null)
                .positiveness(reward < 0 ? reward : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    /**
     * UserDonation 엔티티를 GetUserDonation 응답 DTO로 변환합니다.
     *
     * @param userDonation 사용자 기부 엔티티
     * @return 변환된 GetUserDonation DTO
     */
    private GetUserDonation convertToGetUserDonationResponses(UserDonation userDonation) {

        Donation donation = userDonation.getDonation();

        return new GetUserDonation(
                donation.getId(), donation.getDonationName(), userDonation.getDonationCnt(),
                userDonation.getDonationSum(), userDonation.getCreatedAt()
        );
    }
}
