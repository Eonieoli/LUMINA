package com.lumina.backend.post.service;

import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.UploadPostResponse;

import java.io.IOException;
import java.util.Map;

public interface PostService {

    UploadPostResponse uploadPost(Long userId, UploadPostRequest request) throws IOException;

    Map<String, Object> getPosts(Long myId, Long userId, String categoryName, String feedType, int pageNum);

    void deletePost(Long userId, String role, Long postId);

    Boolean toggleLike(Long userId, Long photoId);

    Map<String, Object> getSubscribePost(Long userId, int pageNum);

    Map<String, Object> searchPost(Long userId, String keyword, int pageNum);
}
