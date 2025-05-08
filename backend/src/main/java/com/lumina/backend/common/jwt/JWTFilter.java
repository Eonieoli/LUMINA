package com.lumina.backend.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.service.CustomHttpServletRequestWrapper;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.TokenValidationUtil;
import com.lumina.backend.user.model.dto.CustomOAuth2User;
import com.lumina.backend.user.model.dto.UserDto;
import com.lumina.backend.user.service.OAuthService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - OncePerRequestFilter를 상속하여 요청마다 한 번만 필터 실행
 * - 특정 URL 및 메서드에 대해선 필터 제외 처리
 */
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final JWTUtil jwtUtil;
    private final TokenValidationUtil tokenValidationUtil;

    private final OAuthService oAuthService;


    /**
     * 필터를 적용하지 않을 요청을 정의하는 메서드
     *
     * @param request HttpServletRequest
     * @return true이면 필터를 건너뜀, false이면 필터 적용
     */
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) throws ServletException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String type = request.getParameter("type");

        return path.equals("/")
                || path.startsWith("/actuator/")
                || path.startsWith("/api/v1/dev/")
                || (path.equals("/api/v1/user")
                && "GET".equalsIgnoreCase(method)
                && ("google".equalsIgnoreCase(type) || "kakao".equalsIgnoreCase(type)));
    }


    /**
     * JWT 토큰을 검증하고 인증 처리를 수행하는 필터 메서드입니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest requestToUse = request;
        try {

            String accessToken = CookieUtil.getCookieValue(request, "access");

            // 개발용
            if (accessToken == null) {
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    accessToken = authorizationHeader.substring(7); // "Bearer " 부분을 제외하고 토큰만 추출
                }
            }
            // 여기까지

            tokenValidationUtil.validateAccessToken(accessToken);

            try {
                jwtUtil.isExpired(accessToken);
            } catch (ExpiredJwtException e) {
                try {
                    String newAccessToken = oAuthService.reissue(request, response);

                    CustomHttpServletRequestWrapper updatedRequest = new CustomHttpServletRequestWrapper(request);
                    updatedRequest.addHeader("Authorization", "Bearer " + newAccessToken); // 헤더 추가
                    updatedRequest.updateCookie("access", newAccessToken); // 쿠키 업데이트
                    accessToken = newAccessToken;
                    requestToUse = updatedRequest; // 재발급된 경우 updatedRequest를 사용
                } catch (CustomException ce) {
                    response.setStatus(ce.getStatus().value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(BaseResponse.error(ce.getMessage())));
                    return;
                }
            }

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(new UserDto(
                    jwtUtil.getSocialId(accessToken),
                    jwtUtil.getNickname(accessToken),
                    jwtUtil.getRole(accessToken)
            ));

            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (CustomException ex) {
            response.setStatus(ex.getStatus().value());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(BaseResponse.error(ex.getMessage())));
            return;
        }

        filterChain.doFilter(requestToUse, response);
    }
}
