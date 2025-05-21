package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.service.TokenService;
import com.lumina.backend.common.utill.*;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final FindUtil findUtil;
    private final TokenUtil tokenUtil;
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
     * @return String 새로 발급된 accessToken
     * @throws CustomException 토큰 검증 실패 시 예외 발생
     */
    @Override
    public String reissue(
            HttpServletRequest request, HttpServletResponse response) {
        System.out.println("토큰 재발급 로직 안으로 들어감");
        String refresh = CookieUtil.getCookieValue(request, "refresh");
        System.out.println("refresh = " + refresh);
        tokenValidationUtil.validateRefreshToken(refresh);
        System.out.println("refresh토큰 검증 완료");
        String nickname = jwtUtil.getNickname(refresh);
        Long userId = userRepository.findIdByNickname(nickname);
        String userKey = redisUtil.getRefreshKey(request, userId);
        String role = tokenUtil.findRoleByToken(request);
        System.out.println("nickname = " + nickname);
        tokenValidationUtil.validateStoredRefreshToken(userKey, refresh);
        System.out.println("refresh토큰 2차검증 완료");
        return tokenService.reissueTokens(userKey, nickname, role, response).get("access");
    }


    /**
     * 사용자 계정 탈퇴 처리 및 관련 리소스 정리
     *
     * @param userId 탈퇴할 사용자 ID
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체 (쿠키 삭제에 사용)
     * @throws CustomException 사용자 미존재 시 예외 발생
     */
    @Override
    @Transactional
    public void deleteUser(
            Long userId, HttpServletRequest request, HttpServletResponse response) {

        User user = findUtil.getUserById(userId);
        // soft delete 방식(상태값 변경 등) 적용
        user.deleteUser();
        userRepository.save(user);

        // 랭킹 ZSet에서 사용자 제거 (ex: 점수 랭킹)
        redisUtil.removeUserFromZSet("sum-point:rank", "user:" + userId);

        // Redis에 저장된 refresh 토큰 삭제
        String userKey = redisUtil.getRefreshKey(request, userId);
        redisUtil.delete(userKey);

        CookieUtil.deleteCookie(response, "access");
        CookieUtil.deleteCookie(response, "refresh");
    }
}
