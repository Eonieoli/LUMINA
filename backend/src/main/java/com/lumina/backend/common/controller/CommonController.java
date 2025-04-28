package com.lumina.backend.common.controller;

import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.common.model.entity.BaseEntity;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class CommonController {

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

    @GetMapping("/token")
    public ResponseEntity<BaseResponse<Void>> getToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String nickname = "dev";
        String role = "ROLE_USER";

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
        response.addCookie(oAuthService.createCookie("access", access));
        response.addCookie(oAuthService.createCookie("refresh", refresh));

        //인증 성공 후 리다이렉트
        response.sendRedirect(successURL);

        return ResponseEntity.ok(BaseResponse.withMessage("개발용 토큰 발급 완료"));
    }
}
