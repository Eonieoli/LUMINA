package com.lumina.backend.common.service;

import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Value("${jwt.access.exp}")
    private String jwtAccessExp;

    @Value("${jwt.refresh.exp}")
    private String jwtRefreshExp;

    @Value("${jwt.redis.exp}")
    private String jwtRedisExp;


    /**
     * 새로운 Access/Refresh 토큰을 발급하고, Redis 및 쿠키에 저장합니다.
     *
     * @param userKey   Redis에 저장할 키
     * @param nickname  사용자 닉네임
     * @param role      사용자 권한
     * @param response  HttpServletResponse (쿠키 설정에 사용)
     * @return          발급된 access/refresh 토큰 Map
     */
    public Map<String, String> reissueTokens(
            String userKey, String nickname,
            String role, HttpServletResponse response) {

        String newAccess = jwtUtil.createJwt("access", nickname, role, Long.parseLong(jwtAccessExp));
        String newRefresh = jwtUtil.createJwt("refresh", nickname, role, Long.parseLong(jwtRefreshExp));

        redisUtil.setex(userKey, newRefresh, Long.parseLong(jwtRedisExp));

        response.addCookie(CookieUtil.createCookie("access", newAccess));
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh));

        return Map.of(
                "access", newAccess,
                "refresh", newRefresh
        );
    }
}
