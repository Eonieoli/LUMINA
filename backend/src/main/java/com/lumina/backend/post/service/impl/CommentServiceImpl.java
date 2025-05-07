package com.lumina.backend.post.service.impl;

import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.service.S3Service;
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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;


    @Override
    @Transactional
    public UploadCommentResponse uploadComment(Long userId, Long postId, UploadCommentRequest request) {

        if (request.getCommentContent() == null || request.getCommentContent().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다. 게시물 ID: " + postId));

        Comment comment;
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + request.getParentCommentId()));
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "부모 댓글의 게시물 불일치");
            }

            comment = new Comment(user, post, parentComment, request.getCommentContent());
        } else {
            comment = new Comment(user, post, request.getCommentContent());
        }
        Comment saveComment = commentRepository.save(comment);

        return new UploadCommentResponse(saveComment.getId());
    }


    @Override
    public Map<String, Object> getComment(Long userId, Long postId, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageRequest);

        List<GetCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> {
                    User user = comment.getUser();
                    int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
                    int childCommentCnt = commentRepository.countByParentCommentId(comment.getId());
                    Boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

                    return new GetCommentResponse(
                            comment.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            comment.getCommentContent(), likeCnt, childCommentCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", commentPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("comments", comments);

        return result;
    }


    @Override
    public List<GetChildCommentResponse> getChildComment(
            Long userId, Long postId, Long ParentCommentId) {

        List<Comment> comments = commentRepository.findByPostIdAndParentCommentId(postId, ParentCommentId);

        List<GetChildCommentResponse> commentResponseList = comments.stream()
                .map(comment -> {
                    User user = comment.getUser();
                    int likeCnt = commentLikeRepository.countByCommentId(comment.getId());
                    Boolean isLike = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());

                    return new GetChildCommentResponse(
                            comment.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            comment.getCommentContent(), likeCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        return commentResponseList;
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

        // 게시물 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시물을 찾을수 없음: " + postId ));

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "댓글을 찾을수 없음: " + commentId ));

        // 해당 댓글이 해당 게시물의 댓글이 아닌 경우 에러 반환
        if (!comment.getPost().equals(post)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게시글의 댓글이 아닙니다");
        }

        // 요청한 사용자 ID가 게시물 소유자가 아닐 경우 에러 반환
        if (role.equals("ROLE_USER") && !comment.getUser().getId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        // mySQL에서 삭제
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

        if (postId == null || postId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 게시물 ID입니다.");
        }

        if (commentId == null || commentId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 댓글 ID입니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 게시물을 찾을 수 없습니다. 게시물 ID: " + postId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. 댓글 ID: " + commentId));

        // 해당 댓글이 해당 게시물의 댓글이 아닌 경우 에러 반환
        if (!comment.getPost().equals(post)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게시글의 댓글이 아닙니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        CommentLike existCommentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElse(null);

        if (existCommentLike != null) {
            // 기존 좋아요 관계가 있으면 좋아요 취소
            commentLikeRepository.delete(existCommentLike);
            user.updateUserLikeCnt(-1);
            return false;
        } else {
            CommentLike commentLike = new CommentLike(user, comment);
            commentLikeRepository.save(commentLike);
            user.updateUserLikeCnt(1);
        }
        userRepository.save(user);
        return true;
    }
}
