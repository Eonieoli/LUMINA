package com.lumina.backend.lumina.service;

import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.request.UploadPostRequest;

public interface LuminaService {

    UploadCommentRequest getPostLumina(Long userId, Long postId);

    UploadCommentRequest getCommentLumina(Long userId, Long commentId);

    void getAiDonation(Long userId);

    void getPostCategory(Long postId, UploadPostRequest request);
}
