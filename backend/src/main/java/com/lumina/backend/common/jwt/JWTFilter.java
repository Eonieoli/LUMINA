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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        // 인증이 필요 없는 경로 및 조건 처리
        return path.equals("/")
                || path.startsWith("/actuator/")
                || path.startsWith("/api/v1/dev/")
                || (path.equals("/api/v1/lumina/post") && "POST".equalsIgnoreCase(method))
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
                    // 만료된 경우 재발급 시도
                    String newAccessToken = oAuthService.reissue(request, response);

                    // 새 토큰으로 request 업데이트
                    CustomHttpServletRequestWrapper updatedRequest = new CustomHttpServletRequestWrapper(request);
                    updatedRequest.addHeader("Authorization", "Bearer " + newAccessToken);
                    updatedRequest.updateCookie("access", newAccessToken);
                    accessToken = newAccessToken;
                    requestToUse = updatedRequest;
                } catch (CustomException ce) {
                    response.setStatus(ce.getStatus().value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(BaseResponse.error(ce.getMessage())));
                    return;
                }
            }

            // 인증 객체 생성 및 SecurityContext에 저장
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
