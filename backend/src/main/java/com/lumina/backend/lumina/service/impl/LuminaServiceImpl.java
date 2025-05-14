package com.lumina.backend.lumina.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.model.entity.UserDonation;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.donation.repository.UserDonationRepository;
import com.lumina.backend.lumina.model.response.EvaluateCommentResponse;
import com.lumina.backend.lumina.model.response.EvaluatePostResponse;
import com.lumina.backend.lumina.model.response.GetCategoryResponse;
import com.lumina.backend.lumina.service.LuminaService;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.CommentLike;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostLike;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.repository.CommentLikeRepository;
import com.lumina.backend.post.repository.PostLikeRepository;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LuminaServiceImpl implements LuminaService {

    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CategoryRepository categoryRepository;
    private final DonationRepository donationRepository;
    private final UserDonationRepository userDonationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final FindUtil findUtil;

    private final WebClient webClient;

    @Value("${LUMINA_POST}")
    private String luminaPost;

    @Value("${LUMINA_COMMENT}")
    private String luminaComment;

    @Value("${GEMMA_CATEGORY}")
    private String gemmaCategory;

    @Value("${GEMMA_POST_CATEGORY}")
    private String gemmaPostCategory;


    /**
     * 게시글에 대한 루미나 평가 결과를 조회합니다.
     *
     * @param userId 평가를 요청한 사용자 ID
     * @param postId 평가 대상 게시글 ID
     * @return UploadCommentRequest 루미나 평가 결과(보상 등)
     */
    @Override
    public UploadCommentRequest getPostLumina(
            Long userId, Long postId) {

        Post post = findUtil.getPostById(postId);
        String nickname = userRepository.findNicknameByUserId(userId);

        Map<String, String> requestPayload = createRequestPayload(
                userId, postId, nickname, post.getPostContent());
        EvaluatePostResponse response = requestLuminaEvaluation(requestPayload);

        return new UploadCommentRequest(response.getReward(), null);
    }


    /**
     * 댓글에 대한 루미나 평가 결과를 조회합니다.
     *
     * @param userId 평가를 요청한 사용자 ID
     * @param commentId 평가 대상 댓글 ID
     * @return UploadCommentRequest 루미나 평가 결과(답글 등)
     */
    @Override
    public UploadCommentRequest getCommentLumina(
            Long userId, Long commentId) {

        Comment comment = findUtil.getCommentById(commentId);
        String nickname = userRepository.findNicknameByUserId(userId);

        Map<String, String> requestPayload = createCommentRequestPayload(userId, commentId, nickname, comment.getCommentContent());
        EvaluateCommentResponse response = requestLuminaCommentEvaluation(requestPayload);

        // 대댓글일 경우 부모 댓글 ID 반환
        Long parentCommentId = (comment.getParentComment() != null)
                ? comment.getParentComment().getId()
                : commentId;

        return new UploadCommentRequest(response.getReply(), parentCommentId);
    }


    /**
     * 사용자의 AI 기부 추천 내역을 생성합니다.
     * 최근 좋아요한 게시글/댓글을 기반으로 카테고리 내 기부 내역을 추천합니다.
     *
     * @param userId 추천을 생성할 사용자 ID
     */
    @Override
    @Transactional
    public void getAiDonation(Long userId) {

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostLike> postLikePage = postLikeRepository.findByUserId(userId, pageRequest);
        Page<CommentLike> commentLikePage = commentLikeRepository.findByUserId(userId, pageRequest);

        // 요청 텍스트 생성
        Map<String, List<String>> requestPayload = new HashMap<>();
        List<String> post = postLikePage.getContent().stream()
                .map(like -> like.getPost().getCategory().getCategoryName())
                .collect(Collectors.toList());
        List<String> comment = commentLikePage.getContent().stream()
                .map(like -> like.getComment().getCommentContent())
                .collect(Collectors.toList());
        requestPayload.put("post", post);
        requestPayload.put("comment", comment);

        // AI 평가 서버 호출 부분
        GetCategoryResponse response = requestRecommendCategory(requestPayload);

        Long categoryId = categoryRepository.findIdByCategoryName(response.getCategoryName());
        List<Donation> donationList = donationRepository.findByCategoryId(categoryId);

        // 기존 AI 추천 내역 삭제
        userDonationRepository.deleteByUserIdAndRegistration(userId, "AI");

        User user = findUtil.getUserById(userId);

        // 새로운 AI 추천 기부 내역 저장
        for (Donation donation : donationList) {
            UserDonation userDonation = new UserDonation(user, donation, "AI");
            userDonationRepository.save(userDonation);
        }

        // 사용자 좋아요 카운트 초기화
        user.resetUserLickCnt();
        userRepository.save(user);
    }


    /**
     * 게시글 카테고리를 추가해준다.
     *
     * @param request 게시물 관련 요청값
     */
    @Override
    @Transactional
    public void getPostCategory(
            Long postId, UploadPostRequest request) {

        Post post = findUtil.getPostById(postId);

        Map<String, Object> requestPayload = new HashMap<>();
        if (request.getPostImageFile() != null && !request.getPostImageFile().isEmpty()) {
            requestPayload.put("postImageFile", request.getPostImageFile());
        }
        requestPayload.put("postContent", request.getPostContent());

        GetCategoryResponse response = requestCategory(requestPayload);
        Category category = findUtil.getCategoryByCategoryName(response.getCategoryName());

        post.updateCategory(category);
        postRepository.save(post);
    }


    /**
     * 게시글 평가 요청에 필요한 Payload를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @param nickname 사용자 닉네임
     * @param postContent 게시글 내용
     * @return Map<String, String> 요청 Payload
     */
    private Map<String, String> createRequestPayload(Long userId, Long postId, String nickname, String postContent) {
        Map<String, String> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("nickname", nickname);
        payload.put("post_content", postContent);
        payload.put("post_id", postId.toString());
        return payload;
    }

    /**
     * 루미나 게시글 평가 서버에 요청을 보내고 응답을 반환합니다.
     *
     * @param requestPayload 평가 요청 데이터
     * @return EvaluatePostResponse 평가 결과
     * @throws CustomException 서버 응답이 없을 때 예외 발생
     */
    private EvaluatePostResponse requestLuminaEvaluation(Map<String, String> requestPayload) {
        EvaluatePostResponse response = webClient.post()
                .uri(luminaPost)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(EvaluatePostResponse.class)
                .block(); // 동기 호출

        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "평가 서버에서 응답이 없습니다.");
        }

        return response;
    }

    /**
     * 댓글 평가 요청에 필요한 Payload를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param commentId 댓글 ID
     * @param nickname 사용자 닉네임
     * @param content 댓글 내용
     * @return Map<String, String> 요청 Payload
     */
    private Map<String, String> createCommentRequestPayload(Long userId, Long commentId, String nickname, String content) {
        Map<String, String> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("nickname", nickname);
        payload.put("comment_content", content);
        payload.put("comment_id", commentId.toString());
        return payload;
    }

    /**
     * 루미나 댓글 평가 서버에 요청을 보내고 응답을 반환합니다.
     *
     * @param requestPayload 평가 요청 데이터
     * @return EvaluateCommentResponse 평가 결과
     * @throws CustomException 서버 응답이 없을 때 예외 발생
     */
    private EvaluateCommentResponse requestLuminaCommentEvaluation(Map<String, String> requestPayload) {
        EvaluateCommentResponse response = webClient.post()
                .uri(luminaComment)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(EvaluateCommentResponse.class)
                .block();

        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 평가 서버에서 응답이 없습니다.");
        }

        return response;
    }

    /**
     * 기부처 추천 서버에 요청을 보내고 응답을 반환합니다.
     *
     * @param requestPayload 기부처 추천 요청 데이터
     * @return GetCategoryResponse 추천 결과
     * @throws CustomException 서버 응답이 없을 때 예외 발생
     */
    private GetCategoryResponse requestRecommendCategory(Map<String, List<String>> requestPayload) {

        GetCategoryResponse response = webClient.post()
                .uri(gemmaCategory)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(GetCategoryResponse.class)
                .block(); // 동기 방식

        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "카테고리 추천 서버에서 응답이 없습니다.");
        }

        return response;
    }

    /**
     * 게시물 카테고리 요청을 보내고 응답을 반환합니다.
     *
     * @param requestPayload 게시물 요청 데이터
     * @return GetCategoryResponse 게시물 카테고리 응답 결과
     * @throws CustomException 서버 응답이 없을 때 예외 발생
     */
    private GetCategoryResponse requestCategory(Map<String, Object> requestPayload) {

        GetCategoryResponse response = webClient.post()
                .uri(gemmaPostCategory)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(GetCategoryResponse.class)
                .block(); // 동기 방식

        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "카테고리 추천 서버에서 응답이 없습니다.");
        }

        return response;
    }
}
