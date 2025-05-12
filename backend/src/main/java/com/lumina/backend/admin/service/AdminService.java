package com.lumina.backend.admin.service;

import java.util.Map;

public interface AdminService {

    Map<String, Object> getUser(Long userId, int pageNum);

    void deleteUser(Long myId, Long userId);

    Map<String, Object> getCurUser(Long userId, int pageNum);

    Map<String, Object> getUserPost(Long myId, Long userId, int pageNum);

    Map<String, Object> getUserComment(Long myId, Long userId, int pageNum);

    void deletePost(Long userId, Long postId);

    void deleteComment(Long userId, Long postId, Long commentId);
}
