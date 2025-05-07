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

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final TokenUtil tokenUtil;

    @Value("${jwt.access.exp}")
    private String jwtAccessExp;

    @Value("${jwt.refresh.exp}")
    private String jwtRefreshExp;

    @Value("${jwt.redis.exp}")
    private String jwtRedisExp;

    public String reissueTokens(
            String userKey, String nickname,
            HttpServletRequest request, HttpServletResponse response) {

        String role = tokenUtil.findRoleByToken(request);

        String newAccess = jwtUtil.createJwt("access", nickname, role, Long.parseLong(jwtAccessExp));
        String newRefresh = jwtUtil.createJwt("refresh", nickname, role, Long.parseLong(jwtRefreshExp));

        redisUtil.setex(userKey, newRefresh, Long.parseLong(jwtRedisExp));

        response.addCookie(CookieUtil.createCookie("access", newAccess));
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh));

        return newAccess;
    }
}
