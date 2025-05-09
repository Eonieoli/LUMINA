package com.lumina.backend.lumina.service.impl;

import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.model.entity.UserDonation;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.donation.repository.UserDonationRepository;
import com.lumina.backend.lumina.model.response.EvaluateCommentResponse;
import com.lumina.backend.lumina.model.response.EvaluatePostResponse;
import com.lumina.backend.lumina.service.LuminaService;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.CommentLike;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostLike;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.repository.CommentLikeRepository;
import com.lumina.backend.post.repository.CommentRepository;
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

    private final FindUtil findUtil;

    private final WebClient webClient;

    @Value("${LUMINA_POST}")
    private String luminaPost;

    @Value("${LUMINA_COMMENT}")
    private String luminaComment;


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


    @Override
    public UploadCommentRequest getCommentLumina(
            Long userId, Long commentId) {

        Comment comment = findUtil.getCommentById(commentId);
        String nickname = userRepository.findNicknameByUserId(userId);

        Map<String, String> requestPayload = createCommentRequestPayload(userId, commentId, nickname, comment.getCommentContent());
        EvaluateCommentResponse response = requestLuminaCommentEvaluation(requestPayload);

        Long parentCommentId = (comment.getParentComment() != null)
                ? comment.getParentComment().getId()
                : commentId;

        return new UploadCommentRequest(response.getReply(), parentCommentId);
    }


    @Override
    @Transactional
    public void getAiDonation(Long userId) {

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostLike> postLikePage = postLikeRepository.findByUserId(userId, pageRequest);
        Page<CommentLike> commentLikePage = commentLikeRepository.findByUserId(userId, pageRequest);

        Map<String, List<String>> requestText = new HashMap<>();
        List<String> post = postLikePage.getContent().stream()
                .map(like -> like.getPost().getPostContent())
                .collect(Collectors.toList());

        List<String> comment = commentLikePage.getContent().stream()
                .map(like -> like.getComment().getCommentContent())
                .collect(Collectors.toList());

        requestText.put("post", post);
        requestText.put("comment", comment);

        // POST 요청 보내고 응답 받기
//        EvaluateCommentResponse response = webClient.post()
//                .uri(luminaComment)
//                .bodyValue(requestText)
//                .retrieve()
//                .bodyToMono(EvaluateCommentResponse.class)
//                .block(); // 동기 방식

        Long categoryId = categoryRepository.findIdByCategoryName("한부모");
        List<Donation> donationList = donationRepository.findByCategoryId(categoryId);

        userDonationRepository.deleteByUserIdAndRegistration(userId, "AI");

        User user = findUtil.getUserById(userId);

        for (Donation donation : donationList) {
            UserDonation userDonation = new UserDonation(user, donation, "AI");
            userDonationRepository.save(userDonation);
        }

        user.resetUserLickCnt();
        userRepository.save(user);
    }


    private Map<String, String> createRequestPayload(Long userId, Long postId, String nickname, String postContent) {
        Map<String, String> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("nickname", nickname);
        payload.put("post_content", postContent);
        payload.put("post_id", postId.toString());
        return payload;
    }

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

    private Map<String, String> createCommentRequestPayload(Long userId, Long commentId, String nickname, String content) {
        Map<String, String> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("nickname", nickname);
        payload.put("comment_content", content);
        payload.put("comment_id", commentId.toString());
        return payload;
    }

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
}
