package com.lumina.backend.post.service;

import com.lumina.backend.post.model.request.UploadPostRequest;

import java.io.IOException;

/**
 * 게시물 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface PostService {

    /**
     * 게시물을 업로드하는 메서드
     */
    void uploadPost(Long userId, UploadPostRequest request) throws IOException;
}
