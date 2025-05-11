package com.lumina.backend.post.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.service.AiService;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.PagingResponseUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.CommentLike;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.GetCommentResponse;
import com.lumina.backend.post.model.response.UploadCommentResponse;
import com.lumina.backend.post.repository.*;
import com.lumina.backend.post.service.CommentService;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    private final FindUtil findUtil;

    private final AiService aiService;


    /**
     * 댓글을 등록합니다.
     *
     * @param userId   댓글 작성자 ID
     * @param postId   댓글이 달릴 게시글 ID
     * @param request  댓글 등록 요청 DTO
     * @return UploadCommentResponse 등록된 댓글 ID 응답
     */
    @Override
    @Transactional
    public UploadCommentResponse uploadComment(
            Long userId, Long postId, UploadCommentRequest request) {

        ValidationUtil.validateRequiredField(request.getCommentContent(), "댓글 내용");

        User user = findUtil.getUserById(userId);
        Post post = findUtil.getPostById(postId);

        int appliedReward = aiService.textReward(user, request.getCommentContent()); // AI 기반 보상 계산
        Comment comment = createComment(user, post, appliedReward, request);
        Comment savedComment = commentRepository.save(comment);

        return new UploadCommentResponse(savedComment.getId());
    }


    /**
     * 게시글의 댓글 목록을 페이징 조회합니다.
     *
     * @param userId   현재 사용자 ID(좋아요 여부 확인용)
     * @param postId   게시글 ID
     * @param pageNum  페이지 번호
     * @return Map<String, Object> 페이징 처리된 댓글 목록
     */
    @Override
    public Map<String, Object> getComment(
            Long userId, Long postId, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageRequest);

        List<GetCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> toGetCommentResponse(userId, comment))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(commentPage, pageNum, "comments", comments);
    }


    /**
     * 대댓글(자식 댓글) 목록을 조회합니다.
     *
     * @param userId          현재 사용자 ID(좋아요 여부 확인용)
     * @param postId          게시글 ID
     * @param ParentCommentId 부모 댓글 ID
     * @return List<GetChildCommentResponse> 대댓글 목록
     */
    @Override
    public List<GetChildCommentResponse> getChildComment(
            Long userId, Long postId, Long ParentCommentId) {

        List<Comment> comments = commentRepository.findByPostIdAndParentCommentId(postId, ParentCommentId);

        return comments.stream()
                .map(comment -> toGetChildCommentResponse(userId, comment))
                .collect(Collectors.toList());
    }


    /**
     * 특정 댓글을 삭제합니다.
     *
     * @param userId    삭제 요청자 ID
     * @param role      삭제 요청자 역할(권한)
     * @param postId    댓글이 속한 게시글 ID
     * @param commentId 삭제할 댓글 ID
     */
    @Override
    @Transactional
    public void deleteComment(
            Long userId, String role, Long postId, Long commentId) {

        Post post = findUtil.getPostById(postId);
        Comment comment = findUtil.getCommentById(commentId);

        ValidationUtil.validateComment(comment, postId); // 댓글이 해당 게시글에 속하는지 검증
        ValidationUtil.validateCommentDelete(role, comment, userId); // 삭제 권한 검증

        commentRepository.delete(comment);
    }


    /**
     * 댓글 좋아요를 토글합니다.
     *
     * @param userId    좋아요 요청자 ID
     * @param postId    댓글이 속한 게시글 ID
     * @param commentId 좋아요/취소할 댓글 ID
     * @return Boolean 좋아요 true, 취소 false
     */
    @Override
    @Transactional
    public Boolean toggleCommentLike(
            Long userId, Long postId, Long commentId) {

        ValidationUtil.validateId(postId, "게시물");
        ValidationUtil.validateId(commentId, "댓글");

        Post post = findUtil.getPostById(postId);
        Comment comment = findUtil.getCommentById(commentId);
        User user = findUtil.getUserById(userId);

        ValidationUtil.validateComment(comment, postId); // 댓글이 해당 게시글에 속하는지 검증

        // 이미 좋아요 했으면 취소, 아니면 추가
        return commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .map(existingCommentLike -> {
                    commentLikeRepository.delete(existingCommentLike);
                    updateUserLike(user, -1); // 좋아요 수 감소
                    return false;
                })
                .orElseGet(() -> {
                    commentLikeRepository.save(new CommentLike(user, comment));
                    updateUserLike(user, 1); // 좋아요 수 증가
                    return true;
                });
    }


    /**
     * 댓글 또는 대댓글 엔티티 생성
     *
     * @param user           작성자
     * @param post           게시글
     * @param appliedReward  AI 보상 점수
     * @param request        댓글 등록 요청 DTO
     * @return Comment 생성된 댓글 엔티티
     */
    private Comment createComment(
            User user, Post post, int appliedReward, UploadCommentRequest request) {

        // 부모 댓글이 없으면 일반 댓글, 있으면 대댓글
        if (request.getParentCommentId() == null) {
            return new Comment(user, post, request.getCommentContent(), appliedReward);
        }

        Comment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + request.getParentCommentId()
                ));

        ValidationUtil.validateComment(parentComment, post.getId());

        return new Comment(user, post, parentComment, request.getCommentContent(), appliedReward);
    }

    /**
     * 댓글 정보를 응답 DTO로 변환합니다.
     *
     * @param userId   현재 사용자 ID (좋아요 여부 확인용)
     * @param comment  변환할 댓글 엔티티
     * @return GetCommentResponse 댓글 응답 DTO
     */
    private GetCommentResponse toGetCommentResponse(Long userId, Comment comment) {

        User user = comment.getUser();
        int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
        int childCommentCnt = commentRepository.countByParentCommentId(comment.getId());
        boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

        return new GetCommentResponse(
                comment.getId(), user.getId(), user.getNickname(),
                user.getProfileImage(), comment.getCommentContent(),
                likeCnt, childCommentCnt, isLike);
    }

    /**
     * 대댓글 정보를 응답 DTO로 변환합니다.
     *
     * @param userId   현재 사용자 ID (좋아요 여부 확인용)
     * @param comment  변환할 대댓글 엔티티
     * @return GetChildCommentResponse 대댓글 응답 DTO
     */
    private GetChildCommentResponse toGetChildCommentResponse(Long userId, Comment comment) {

        User user = comment.getUser();
        int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
        Boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

        return new GetChildCommentResponse(
                comment.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                comment.getCommentContent(), likeCnt, isLike
        );
    }

    /**
     * 사용자의 좋아요 수를 증감 및 저장합니다.
     *
     * @param user  대상 사용자 엔티티
     * @param value 증감 값 (+1, -1)
     */
    private void updateUserLike(User user, int value) {
        user.updateUserLikeCnt(value);
        userRepository.save(user);
    }
}
