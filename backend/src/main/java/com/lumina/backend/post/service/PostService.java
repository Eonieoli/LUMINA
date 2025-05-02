package com.lumina.backend.post.service;

import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.UploadCommentResponse;
import com.lumina.backend.post.model.response.UploadPostResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 게시물 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface PostService {

    /**
     * 게시물을 업로드하는 메서드
     */
    UploadPostResponse uploadPost(Long userId, UploadPostRequest request) throws IOException;

    Map<String, Object> getPosts(Long myId, Long userId, String categoryName, int pageNum);

    /**
     * 특정 게시물을 삭제하는 메서드
     */
    void deletePost(Long userId, String role, Long postId);

    /**
     * 게시물에 대한 좋아요 토글 메서드
     */
    Boolean toggleLike(Long userId, Long photoId);

    UploadCommentResponse uploadComment(Long userId, Long postId, UploadCommentRequest request);

    Map<String, Object> getComment(Long userId, Long postId, int pageNum);

    List<GetChildCommentResponse> getChildComment(Long userId, Long postId, Long ParentCommentId);

    /**
     * 특정 댓글을 삭제하는 메서드
     */
    void deleteComment(Long userId, String role, Long postId, Long commentId);

    /**
     * 댓글에 대한 좋아요 토글 메서드
     */
    Boolean toggleCommentLike(Long userId, Long photoId, Long commentId);

    Map<String, Object> getSubscribePost(Long userId, int pageNum);

    Map<String, Object> searchPost(Long userId, String keyword, int pageNum);
}
