package com.lumina.backend.user.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
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

/**
 * 인증 관련 서비스를 제공하는 클래스
 */
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final CookieUtil cookieUtil;

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
     * @return ResponseEntity 객체로 결과 반환
     */
    @Override
    public String reissue(
            HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 리프레시 토큰 추출
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                }
            }
        }

        if (refresh == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "RefreshToken 쿠키 없음");
        }

        // 리프레시 토큰 만료 여부 확인
        if (jwtUtil.isExpired(refresh)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰");
        }

        // 토큰 유형 확인
        if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "RefreshToken이 아님");
        }

        // 닉네임 및 Redis 키 생성
        String nickname = jwtUtil.getNickname(refresh);
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String deviceType = redisUtil.getDeviceType(userAgent); // 기기 유형 판별
        String userKey = "refresh:" + userRepository.findIdByNickname(nickname) + ":" + deviceType; // Redis 키 생성;

        // Redis에 저장된 리프레시 토큰 존재 여부 확인
        Boolean isExist = redisUtil.exists(userKey);
        if (!isExist) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Redis에 존재하지 않음");
        }

        // Redis에 저장된 리프레시 토큰 동일 여부 확인
        Object storedRefreshTokenObj = redisUtil.get(userKey);
        String storedRefreshToken = storedRefreshTokenObj.toString();
        if (!storedRefreshToken.equals(refresh)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Redis의 값과 다름");
        }

        // 새로운 액세스 및 리프레시 토큰 생성
        String role = jwtUtil.getRole(refresh);
        String newAccess = jwtUtil.createJwt("access", nickname, role, Long.parseLong(jwtAccessExp)); // 10분 유효
        String newRefresh = jwtUtil.createJwt("refresh", nickname, role, Long.parseLong(jwtRefreshExp)); // 1일 유효

        // Redis에 새 리프레시 토큰 저장
        redisUtil.setex(userKey, newRefresh, Long.parseLong(jwtRedisExp));

        // 클라이언트에 새 토큰 쿠키로 설정
        response.addCookie(cookieUtil.createCookie("access", newAccess));
        response.addCookie(cookieUtil.createCookie("refresh", newRefresh));

        return newAccess;
    }


    /**
     * 사용자 계정을 탈퇴하는 메서드
     *
     * @param userId 탈퇴할 사용자의 ID
     * @param response HTTP 응답 객체 (쿠키 삭제에 사용)
     */
    @Override
    public void deleteUser(
            Long userId, HttpServletRequest request, HttpServletResponse response) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        user.deleteUser();
        userRepository.save(user);

        // Redis에서 rank 삭제
        String rankKey = "sum-point:rank";
        String rankUserKey = "user:" + userId;
        redisUtil.removeUserFromZSet(rankKey, rankUserKey);

        // Redis에서 Refresh Token 삭제
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String deviceType = redisUtil.getDeviceType(userAgent); // 기기 유형 판별
        String userKey = "refresh:" + userId + ":" + deviceType;
        redisUtil.delete(userKey);

        // 쿠키에서 Access Token과 Refresh Token 삭제
        cookieUtil.deleteCookie(response, "access");
        cookieUtil.deleteCookie(response, "refresh");
    }
}
