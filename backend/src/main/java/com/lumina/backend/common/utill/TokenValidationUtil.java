package com.lumina.backend.common.utill;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenValidationUtil {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    public void validateRefreshToken(String refresh) {

        if (refresh == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "RefreshToken 쿠키 없음");
        }

        if (jwtUtil.isExpired(refresh)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰");
        }

        if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "RefreshToken이 아님");
        }
    }

    public void validateStoredRefreshToken(String redisKey, String token) {
        if (!redisUtil.exists(redisKey)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Redis에 존재하지 않음");
        }

        Object storedToken = redisUtil.get(redisKey);
        if (!token.equals(String.valueOf(storedToken))) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Redis의 값과 다름");
        }
    }

    public void validateAccessToken(String access) {

        if (access == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증 토큰 없음");
        }

        if (!"access".equals(jwtUtil.getCategory(access))) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AccessToken이 아님");
        }
    }
}
