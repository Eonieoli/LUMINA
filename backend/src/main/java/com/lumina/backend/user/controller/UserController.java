package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.GetMyProfileResponse;
import com.lumina.backend.user.model.response.GetMyReward;
import com.lumina.backend.user.model.response.GetUserPointResponse;
import com.lumina.backend.user.model.response.GetUserProfileResponse;
import com.lumina.backend.user.service.OAuthService;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 사용자 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final TokenUtil tokenUtil;


    /**
     * 현재 사용자의 프로필 정보를 조회하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (현재 사용자 인증 정보 포함)
     * @return ResponseEntity<BaseResponse < GetMyProfileResponse>> 현재 사용자 프로필 정보 응답
     */
    @GetMapping("/profile/me")
    public ResponseEntity<BaseResponse<GetMyProfileResponse>> getMyProfile(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        GetMyProfileResponse getMyProfileResponse = userService.getMyProfile(userId);

        return ResponseEntity.ok(BaseResponse.success("내 프로필 조회 성공", getMyProfileResponse));
    }


    /**
     * 특정 사용자의 프로필 정보를 조회하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (현재 사용자 인증 정보 포함)
     * @param userId  프로필을 조회할 사용자의 ID
     * @return ResponseEntity<BaseResponse < GetUserProfileResponse>> 사용자 프로필 정보 응답
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<BaseResponse<GetUserProfileResponse>> getUserProfile(
            HttpServletRequest request, @PathVariable Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        GetUserProfileResponse getUserProfileResponse = userService.getUserProfile(myId, userId);

        return ResponseEntity.ok(BaseResponse.success("유저 프로필 조회 성공", getUserProfileResponse));
    }


    /**
     * 현재 사용자의 프로필 정보를 수정하는 엔드포인트
     *
     * @param response               HTTP 응답 객체
     * @param request                HTTP 요청 객체 (현재 사용자 인증 정보 포함)
     * @param updateMyProfileRequest 수정할 프로필 정보
     * @return ResponseEntity<BaseResponse < Void>> 수정 결과 응답
     */
    @PatchMapping("/profile")
    public ResponseEntity<BaseResponse<Void>> updateMyProfile(
            HttpServletResponse response, HttpServletRequest request,
            @ModelAttribute UpdateMyProfileRequest updateMyProfileRequest) throws IOException {

        Long userId = tokenUtil.findIdByToken(request);
        userService.updateMyProfile(userId, request, updateMyProfileRequest, response);

        return ResponseEntity.ok(BaseResponse.withMessage("프로필 수정 완료"));
    }


    /**
     * 현재 사용자의 포인트 정보를 응답하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (현재 사용자 인증 정보 포함)
     * @return ResponseEntity<BaseResponse<GetUserPointResponse>> 사용자 포인트 정보 응답
     */
    @GetMapping("/point")
    public ResponseEntity<BaseResponse<GetUserPointResponse>> getUserPoint(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        GetUserPointResponse response = userService.getUserPoint(userId);

        return ResponseEntity.ok(BaseResponse.success("포인트 조회 성공", response));
    }


    /**
     * 사용자를 검색하는 엔드포인트
     *
     * @param keyword 검색어 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색 결과 응답
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Map<String, Object>>> searchUser(
            @RequestParam String keyword, @RequestParam int pageNum) {

        Map<String, Object> response = userService.searchUser(keyword, pageNum);

        return ResponseEntity.ok(BaseResponse.success("유저 검색 성공", response));
    }


    @GetMapping("/reward")
    public ResponseEntity<BaseResponse<List<GetMyReward>>> getMyReward(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        List<GetMyReward> response = userService.getMyReward(userId);

        return ResponseEntity.ok(BaseResponse.success("리워드 조회 성공", response));
    }
}
