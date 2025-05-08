package com.lumina.backend.common.service;

import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.utill.CookieUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.common.utill.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
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
