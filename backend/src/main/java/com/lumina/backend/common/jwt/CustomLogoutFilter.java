package com.lumina.backend.common.jwt;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.common.utill.TokenValidationUtil;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * 로그아웃 요청을 처리하는 커스텀 필터
 * - Refresh 토큰 검증 및 Redis에서 삭제
 * - Refresh 토큰 쿠키 제거
 * - 로그아웃 성공 시 클라이언트를 리다이렉트
 */
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final TokenValidationUtil tokenValidationUtil;


    /**
     * ServletRequest와 ServletResponse를 HttpServletRequest, HttpServletResponse로 변환하여 실제 doFilter 실행
     */
    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }


    /**
     * 로그아웃 요청 처리 로직
     */
    private void doFilter(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        // 1. 로그아웃 경로 및 POST 요청인지 확인
        String requestUri = request.getRequestURI();
        if (!requestUri.matches(".*/logout$")) {
            filterChain.doFilter(request, response); // 로그아웃 요청이 아니면 필터 체인 계속 진행
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response); // POST 요청이 아니면 필터 체인 계속 진행
            return;
        }

        String refresh = CookieUtil.getCookieValue(request, "refresh");

        tokenValidationUtil.validateRefreshToken(refresh);

        String nickName = jwtUtil.getNickname(refresh);
        Long userId = userRepository.findIdByNickname(nickName);
        String userKey = redisUtil.getRefreshKey(request, userId);
        tokenValidationUtil.validateStoredRefreshToken(userKey, refresh);

        redisUtil.delete(userKey);

        CookieUtil.deleteCookie(response, "access");
        CookieUtil.deleteCookie(response, "refresh");

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
