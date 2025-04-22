package com.lumina.backend.user.controller;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * OAuth 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;


    /**
     * Google 로그인 페이지로 리다이렉트하는 엔드포인트
     *
     * @param response HTTP 응답 객체
     * @throws IOException 리다이렉트 중 발생할 수 있는 입출력 예외
     */
    @GetMapping("")
    public void redirectToLogin(
            @RequestParam String type, HttpServletResponse response) throws IOException {

        if (type.equals("google")) {
            response.sendRedirect("/oauth2/authorization/google");
        } else if (type.equals("kakao")) {
            response.sendRedirect("/oauth2/authorization/kakao");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "지원하지 않는 로그인 타입입니다: " + type);
        }
    }


    /**
     * 사용자 로그아웃을 처리합니다.
     * 로그아웃 완료 메시지를 반환합니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> redirectToLogout() {
        //logoutFilter 에서 로그아웃 진행 후 응답 반환
        return ResponseEntity.ok(BaseResponse.withMessage("로그아웃 완료"));
    }


    /**
     * 회원 탈퇴를 처리합니다.
     * 회원탈퇴 완료 메시지를 반환합니다.
     */
    @GetMapping("/delete")
    public ResponseEntity<BaseResponse<Void>> deleteUser(
            HttpServletRequest request, HttpServletResponse response) {

        Long userId = oAuthService.findIdByToken(request);
        oAuthService.deleteUser(userId, request, response);

        return ResponseEntity.ok(BaseResponse.withMessage("회원탈퇴 완료"));
    }
}

