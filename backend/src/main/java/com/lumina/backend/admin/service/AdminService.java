package com.lumina.backend.admin.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AdminService {

    Map<String, Object> getUser(Long userId, int pageNum);

    void deleteUser(Long myId, Long userId);
}
