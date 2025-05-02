package com.lumina.backend.lumina.service.impl;

import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
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

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CategoryRepository categoryRepository;
    private final DonationRepository donationRepository;
    private final UserDonationRepository userDonationRepository;
    private final UserRepository userRepository;

    private final WebClient webClient;

    @Value("${LUMINA_POST}")
    private String luminaPost;

    @Value("${LUMINA_COMMENT}")
    private String luminaComment;


    @Override
    public UploadCommentRequest getPostLumina(
            Long userId, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다. 게시물 ID: " + postId));

        Map<String, String> request = new HashMap<>();
        request.put("user_id", userId.toString());
        request.put("post_content", post.getPostContent());
        request.put("post_id", postId.toString());

        // POST 요청 보내고 응답 받기
        EvaluatePostResponse response = webClient.post()
                .uri(luminaPost)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EvaluatePostResponse.class)
                .block(); // 동기 방식

        // 응답 null 체크 및 예외 처리
        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "평가 서버에서 응답이 없습니다.");
        }

        return new UploadCommentRequest(response.getReward(), null);
    }


    @Override
    public UploadCommentRequest getCommentLumina(
            Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + commentId));

        Map<String, String> request = new HashMap<>();
        request.put("user_id", userId.toString());
        request.put("comment_content", comment.getCommentContent());
        request.put("comment_id", commentId.toString());

        // POST 요청 보내고 응답 받기
        EvaluateCommentResponse response = webClient.post()
                .uri(luminaComment)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EvaluateCommentResponse.class)
                .block(); // 동기 방식

        // 응답 null 체크 및 예외 처리
        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "평가 서버에서 응답이 없습니다.");
        }

        Long parentCommentId = commentId;
        if (comment.getParentComment() != null) {
            parentCommentId = comment.getParentComment().getId();
        }

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        for (Donation donation : donationList) {
            UserDonation userDonation = new UserDonation(user, donation, "AI");
            userDonationRepository.save(userDonation);
        }

        user.resetUserLickCnt();
        userRepository.save(user);
    }
}
