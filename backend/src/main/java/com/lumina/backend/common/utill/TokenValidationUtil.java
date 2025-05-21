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


    /**
     * Refresh 토큰의 유효성을 검증합니다.
     *
     * @param refresh 검증할 Refresh 토큰
     * @throws CustomException 유효하지 않은 토큰일 경우 예외 발생
     */
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


    /**
     * Redis에 저장된 Refresh 토큰의 유효성을 검증합니다.
     *
     * @param redisKey Redis에 저장된 토큰의 키
     * @param token    클라이언트로부터 전달받은 토큰
     * @throws CustomException 저장된 값과 다르거나 존재하지 않을 경우 예외 발생
     */
    public void validateStoredRefreshToken(String redisKey, String token) {

        if (!redisUtil.exists(redisKey)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Redis에 존재하지 않음");
        }

        Object storedToken = redisUtil.get(redisKey);
        if (!token.equals(String.valueOf(storedToken))) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Redis의 값과 다름");
        }
    }


    /**
     * Access 토큰의 유효성을 검증합니다.
     *
     * @param access 검증할 Access 토큰
     * @throws CustomException 유효하지 않은 토큰일 경우 예외 발생
     */

    public void validateAccessToken(String access) {

        if (access == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증 토큰 없음");
        }
    }
}
