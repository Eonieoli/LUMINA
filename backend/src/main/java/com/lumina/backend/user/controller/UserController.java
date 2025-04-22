package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.user.model.response.GetMyProfileResponse;
import com.lumina.backend.user.service.OAuthService;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 관련 API를 처리하는 컨트롤러
 * - 사용자 정보 조회
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final OAuthService oAuthService;
    private final UserService userService;


    /**
     * 현재 사용자의 프로필 정보를 조회하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (현재 사용자 인증 정보 포함)
     * @return ResponseEntity<BaseResponse < GetMyProfileResponse>> 현재 사용자 프로필 정보 응답
     */
    @GetMapping("/profile/me")
    public ResponseEntity<BaseResponse<GetMyProfileResponse>> getMyProfile(
            HttpServletRequest request) {

        Long userId = oAuthService.findIdByToken(request);
        GetMyProfileResponse getMyProfileResponse = userService.getMyProfile(userId);

        return ResponseEntity.ok(BaseResponse.success("내 프로필 조회 성공", getMyProfileResponse));
    }
}
