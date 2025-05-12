package com.lumina.backend.common.handler;

import com.lumina.backend.common.service.TokenService;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.user.model.dto.CustomOAuth2User;
import com.lumina.backend.user.repository.UserRepository;
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

    private final RedisUtil redisUtil;

    private final TokenService tokenService;

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
            HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 인증된 사용자 정보 추출
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String socialId = customUserDetails.getSocialId();
        String nickname = userRepository.findNicknameBySocialId(socialId);
        String role = userRepository.findRoleBySocialId(socialId);
        String userKey = redisUtil.getRefreshKey(request, userRepository.findIdByNickname(nickname));

        // 토큰 재발급 및 쿠키/Redis 저장
        tokenService.reissueTokens(userKey, nickname, role, response);

        response.sendRedirect(successURL);
    }
}
