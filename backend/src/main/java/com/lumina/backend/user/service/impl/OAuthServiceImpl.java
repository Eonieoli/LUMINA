package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.service.TokenService;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.common.utill.TokenValidationUtil;
import com.lumina.backend.common.utill.UserUtil;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 서비스를 제공하는 클래스
 */
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserUtil userUtil;
    private final TokenValidationUtil tokenValidationUtil;

    private final TokenService tokenService;

    @Value("${JWT_ACCESS_EXP}")
    private String jwtAccessExp;

    @Value("${JWT_REFRESH_EXP}")
    private String jwtRefreshExp;

    @Value("${JWT_REDIS_EXP}")
    private String jwtRedisExp;


    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 및 리프레시 토큰을 발급합니다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return String 생성된 accessToken 응답
     */
    @Override
    public String reissue(
            HttpServletRequest request, HttpServletResponse response) {

        String refresh = CookieUtil.getCookieValue(request, "refresh");

        tokenValidationUtil.validateRefreshToken(refresh);

        String nickname = jwtUtil.getNickname(refresh);
        Long userId = userRepository.findIdByNickname(nickname);
        String userKey = redisUtil.getRefreshKey(request, userId);

        tokenValidationUtil.validateStoredRefreshToken(userKey, refresh);

        return tokenService.reissueTokens(userKey, nickname, request, response);
    }


    /**
     * 사용자 계정을 탈퇴하는 메서드
     *
     * @param userId 탈퇴할 사용자의 ID
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체 (쿠키 삭제에 사용)
     */
    @Override
    @Transactional
    public void deleteUser(
            Long userId, HttpServletRequest request, HttpServletResponse response) {

        User user = userUtil.getUserById(userId);
        user.deleteUser();
        userRepository.save(user);

        redisUtil.removeUserFromZSet("sum-point:rank", "user:" + userId);

        String userKey = redisUtil.getRefreshKey(request, userId);
        redisUtil.delete(userKey);

        // 쿠키에서 Access Token과 Refresh Token 삭제
        CookieUtil.deleteCookie(response, "access");
        CookieUtil.deleteCookie(response, "refresh");
    }
}
