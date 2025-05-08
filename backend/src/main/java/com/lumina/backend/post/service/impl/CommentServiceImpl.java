package com.lumina.backend.post.service.impl;

import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.service.S3Service;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.PagingResponseUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.CommentLike;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostLike;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.GetCommentResponse;
import com.lumina.backend.post.model.response.UploadCommentResponse;
import com.lumina.backend.post.repository.*;
import com.lumina.backend.post.service.CommentService;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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


    @Override
    @Transactional
    public UploadCommentResponse uploadComment(Long userId, Long postId, UploadCommentRequest request) {

        ValidationUtil.validateRequiredField(request.getCommentContent(), "댓글 내용");

        User user = findUtil.getUserById(userId);
        Post post = findUtil.getPostById(postId);

        Comment comment = createComment(user, post, request);
        Comment savedComment = commentRepository.save(comment);

        return new UploadCommentResponse(savedComment.getId());
    }


    @Override
    public Map<String, Object> getComment(Long userId, Long postId, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageRequest);

        List<GetCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> toGetCommentResponse(userId, comment))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(commentPage, pageNum, "comments", comments);
    }


    @Override
    public List<GetChildCommentResponse> getChildComment(
            Long userId, Long postId, Long ParentCommentId) {

        List<Comment> comments = commentRepository.findByPostIdAndParentCommentId(postId, ParentCommentId);

        return comments.stream()
                .map(comment -> toGetChildCommentResponse(userId, comment))
                .collect(Collectors.toList());
    }


    /**
     * 특정 댓글을 삭제하는 메서드
     *
     * @param postId 댓글 게시물의 ID
     * @param commentId 삭제할 댓글의 ID
     * @param userId 삭제 요청을 한 사용자의 ID
     */
    @Override
    @Transactional
    public void deleteComment(
            Long userId, String role, Long postId, Long commentId) {

        Post post = findUtil.getPostById(postId);
        Comment comment = findUtil.getCommentId(commentId);

        ValidationUtil.validateComment(comment, postId);
        ValidationUtil.validateCommentDelete(role, comment, userId);

        commentRepository.delete(comment);
    }


    /**
     * 댓글 좋아요를 토글하는 메서드
     *
     * @param postId     댓글 게시물의 ID
     * @param commentId  좋아요 할 댓글의 ID
     * @return 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     */
    @Override
    @Transactional
    public Boolean toggleCommentLike(
            Long userId, Long postId, Long commentId) {

        ValidationUtil.validateId(postId, "게시물");
        ValidationUtil.validateId(commentId, "댓글");

        Post post = findUtil.getPostById(postId);
        Comment comment = findUtil.getCommentId(commentId);
        User user = findUtil.getUserById(userId);

        ValidationUtil.validateComment(comment, postId);

        return commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .map(existingCommentLike -> {
                    commentLikeRepository.delete(existingCommentLike);
                    updateUserLike(user, -1);
                    return false;
                })
                .orElseGet(() -> {
                    commentLikeRepository.save(new CommentLike(user, comment));
                    updateUserLike(user, 1);
                    return true;
                });
    }


    private Comment createComment(User user, Post post, UploadCommentRequest request) {
        if (request.getParentCommentId() == null) {
            return new Comment(user, post, request.getCommentContent());
        }

        Comment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + request.getParentCommentId()
                ));

        ValidationUtil.validateComment(parentComment, post.getId());

        return new Comment(user, post, parentComment, request.getCommentContent());
    }

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

    private GetChildCommentResponse toGetChildCommentResponse(Long userId, Comment comment) {

        User user = comment.getUser();
        int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
        Boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

        return new GetChildCommentResponse(
                comment.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                comment.getCommentContent(), likeCnt, isLike
        );
    }

    private void updateUserLike(User user, int value) {
        user.updateUserLikeCnt(value);
        userRepository.save(user);
    }
}
