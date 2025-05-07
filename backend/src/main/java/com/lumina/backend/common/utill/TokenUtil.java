package com.lumina.backend.common.utill;

import com.lumina.backend.common.jwt.JWTUtil;
import com.lumina.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;


    /**
     * 현재 로그인한 사용자의 ID를 닉네임을 통해 조회하는 메서드
     *
     * @param request HTTP 요청 객체 (쿠키에서 AccessToken 추출)
     * @return Long 사용자 ID
     */
    public Long findIdByToken(HttpServletRequest request) {

        // 쿠키에서 AccessToken 추출
        String access = CookieUtil.getCookieValue(request, "access");

        //개발용
        if (access == null) {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                access = authorizationHeader.substring(7); // "Bearer " 부분을 제외하고 토큰만 추출
            }
        }
        //여기까지

        // JWT에서 닉네임 추출
        String nickname = jwtUtil.getNickname(access);

        return userRepository.findIdByNickname(nickname);
    }


    /**
     * 현재 로그인한 사용자의 Role을 토큰을 통해 조회하는 메서드
     *
     * @param request HTTP 요청 객체 (쿠키에서 AccessToken 추출)
     * @return String 사용자 Role
     */
    public String findRoleByToken(HttpServletRequest request) {

        // 쿠키에서 AccessToken 추출
        String access = CookieUtil.getCookieValue(request, "access");

        //개발용
        if (access == null) {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                access = authorizationHeader.substring(7); // "Bearer " 부분을 제외하고 토큰만 추출
            }
        }
        //여기까지

        return jwtUtil.getRole(access);
    }
}
