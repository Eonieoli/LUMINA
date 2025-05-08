package com.lumina.backend.post.service;

import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.UploadCommentResponse;

import java.util.List;
import java.util.Map;

public interface CommentService {

    UploadCommentResponse uploadComment(Long userId, Long postId, UploadCommentRequest request);

    Map<String, Object> getComment(Long userId, Long postId, int pageNum);

    List<GetChildCommentResponse> getChildComment(Long userId, Long postId, Long ParentCommentId);

    void deleteComment(Long userId, String role, Long postId, Long commentId);

    Boolean toggleCommentLike(Long userId, Long photoId, Long commentId);
}
