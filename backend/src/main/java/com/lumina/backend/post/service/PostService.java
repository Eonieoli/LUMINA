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

    UploadPostResponse uploadPost(Long userId, UploadPostRequest request) throws IOException;

    Map<String, Object> getPosts(Long myId, Long userId, String categoryName, int pageNum);

    void deletePost(Long userId, String role, Long postId);

    Boolean toggleLike(Long userId, Long photoId);

    Map<String, Object> getSubscribePost(Long userId, int pageNum);

    Map<String, Object> searchPost(Long userId, String keyword, int pageNum);
}
