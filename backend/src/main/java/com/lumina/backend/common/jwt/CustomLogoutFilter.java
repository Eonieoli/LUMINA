package com.lumina.backend.common.jwt;

import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.common.utill.TokenValidationUtil;
import com.lumina.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final TokenValidationUtil tokenValidationUtil;


    /**
     * 로그아웃 요청을 처리하는 필터입니다.
     * 로그아웃 경로로 들어온 POST 요청에 대해 Refresh 토큰을 검증하고,
     * Redis에서 토큰 정보를 삭제한 뒤, 쿠키를 제거합니다.
     *
     * @param request  ServletRequest 객체
     * @param response ServletResponse 객체
     * @param chain    FilterChain 객체
     * @throws IOException 입출력 예외 발생 시
     * @throws ServletException 서블릿 예외 발생 시
     */
    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }


    /**
     * 로그아웃 요청 처리 로직입니다.
     * 로그아웃이 아닌 요청이거나 POST가 아니면 다음 필터로 넘깁니다.
     * 유효한 Refresh 토큰이 있는 경우, Redis와 쿠키에서 토큰을 삭제합니다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param filterChain FilterChain 객체
     * @throws IOException 입출력 예외 발생 시
     * @throws ServletException 서블릿 예외 발생 시
     */
    private void doFilter(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        // 로그아웃 경로가 아니면 다음 필터로 이동
        String requestUri = request.getRequestURI();
        if (!requestUri.matches(".*/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        // POST 요청이 아니면 다음 필터로 이동
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Refresh 토큰 추출 및 검증
        String refresh = CookieUtil.getCookieValue(request, "refresh");
        tokenValidationUtil.validateRefreshToken(refresh);

        // 사용자 정보 조회 및 Redis에서 토큰 검증
        String nickName = jwtUtil.getNickname(refresh);
        Long userId = userRepository.findIdByNickname(nickName);
        String userKey = redisUtil.getRefreshKey(request, userId);
        tokenValidationUtil.validateStoredRefreshToken(userKey, refresh);

        // Redis에서 토큰 삭제
        redisUtil.delete(userKey);

        CookieUtil.deleteCookie(response, "access");
        CookieUtil.deleteCookie(response, "refresh");

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
