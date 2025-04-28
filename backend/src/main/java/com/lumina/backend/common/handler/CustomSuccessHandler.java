package com.lumina.backend.common.handler;

import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.user.model.dto.CustomOAuth2User;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    private final OAuthService oAuthService;

    @Value("${LOGIN_SUCCESS}")
    private String successURL;

    @Value("${JWT_ACCESS_EXP}")
    private String jwtAccessExp;

    @Value("${JWT_REFRESH_EXP}")
    private String jwtRefreshExp;

    @Value("${JWT_REDIS_EXP}")
    private String jwtRedisExp;


    /**
     * OAuth2 인증 성공 시 호출됩니다.
     * JWT 토큰을 생성하고 Redis에 Refresh 토큰을 저장한 후, 클라이언트에 쿠키를 설정합니다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 객체 (OAuth2User 정보 포함)
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // OAuth2User 정보 가져오기
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String socialId = customUserDetails.getSocialId();
        String nickname = userRepository.findNicknameBySocialId(socialId);
        String role = userRepository.findRoleBySocialId(socialId);

        // 기기 정보 가져오기
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String deviceType = oAuthService.getDeviceType(userAgent); // 기기 유형 판별

        // Access Token 및 Refresh Token 생성
        String access = jwtUtil.createJwt("access", nickname, role, Long.parseLong(jwtAccessExp)); // 10분 유효
        String refresh = jwtUtil.createJwt("refresh", nickname, role, Long.parseLong(jwtRefreshExp)); // 1일 유효

        // Redis에 Refresh Token 저장
        String userKey = "refresh:" + userRepository.findIdByNickname(nickname) + ":" + deviceType;
        redisUtil.setex(userKey, refresh, Long.parseLong(jwtRedisExp)); // 1일 TTL

        // 클라이언트에 Access Token 및 Refresh Token 쿠키로 설정
//        response.addCookie(oAuthService.createCookie("access", access));
//        response.addCookie(oAuthService.createCookie("refresh", refresh));

        // 개발용 쿠키 헤더에 설정
        String accessCookie = "access=" + access + "; Path=/; HttpOnly; Secure; SameSite=None; Domain=localhost; Max-Age=86400";
        String refreshCookie = "refresh=" + refresh + "; Path=/; HttpOnly; Secure; SameSite=None; Domain=localhost; Max-Age=86400";

        response.setHeader("Set-Cookie", accessCookie);
        response.addHeader("Set-Cookie", refreshCookie);
        // 여기까지

        //인증 성공 후 리다이렉트
        response.sendRedirect(successURL);
    }
}
