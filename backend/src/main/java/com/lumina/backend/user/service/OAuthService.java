package com.lumina.backend.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuthService {

    String reissue(HttpServletRequest request, HttpServletResponse response);

    void deleteUser(Long userId, HttpServletRequest request, HttpServletResponse response);
}
