package com.lumina.backend.common.jwt;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
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
    private final CookieUtil cookieUtil;


    /**
     * ServletRequest와 ServletResponse를 HttpServletRequest, HttpServletResponse로 변환하여 실제 doFilter 실행
     */
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }


    /**
     * 로그아웃 요청 처리 로직
     */
    private void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
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

        // 2. 쿠키에서 Refresh 토큰 추출
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue(); // refresh 쿠키 값 추출
                }
            }
        }

        // Refresh 토큰이 없으면 Bad Request 응답
        if (refresh == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Refresh 토큰 없음");
        }

        // 3. 토큰 만료 여부 확인
        try {
            jwtUtil.isExpired(refresh); // 만료되면 예외 발생
        } catch (ExpiredJwtException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Refresh 토큰 만료");
        }

        // 4. 토큰의 카테고리 확인 ("refresh"가 아니면 잘못된 요청)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Refresh 토큰이 아님");
        }

        // 5. Redis에서 토큰 존재 여부 확인
        String nickName = jwtUtil.getNickname(refresh); // 토큰에서 닉네임 추출
        String userAgent = request.getHeader("User-Agent").toLowerCase(); // 기기 정보 추출
        String deviceType = redisUtil.getDeviceType(userAgent); // PC 또는 모바일 여부 판별
        String userKey = "refresh:" + userRepository.findIdByNickname(nickName) + ":" + deviceType; // Redis 키 조합

        Boolean isExist = redisUtil.exists(userKey); // Redis에 존재하는지 확인
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 6. Redis에서 해당 키 삭제 (토큰 무효화)
        redisUtil.delete(userKey);

        // 7. access 및 refresh 쿠키 삭제
        cookieUtil.deleteCookie(response, "access");
        cookieUtil.deleteCookie(response, "refresh");

        // 8. 로그아웃 성공 상태 코드 전송
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
