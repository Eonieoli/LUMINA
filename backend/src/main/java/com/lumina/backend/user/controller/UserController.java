package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.user.model.request.UpdateMyProfileRequest;
import com.lumina.backend.user.model.response.*;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final TokenUtil tokenUtil;


    @GetMapping("/profile/me")
    public ResponseEntity<BaseResponse<GetMyProfileResponse>> getMyProfile(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        GetMyProfileResponse getMyProfileResponse = userService.getMyProfile(userId);

        return ResponseEntity.ok(BaseResponse.success("내 프로필 조회 성공", getMyProfileResponse));
    }


    @GetMapping("/profile/{userId}")
    public ResponseEntity<BaseResponse<GetUserProfileResponse>> getUserProfile(
            HttpServletRequest request, @PathVariable Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        GetUserProfileResponse getUserProfileResponse = userService.getUserProfile(myId, userId);

        return ResponseEntity.ok(BaseResponse.success("유저 프로필 조회 성공", getUserProfileResponse));
    }


    @PatchMapping("/profile")
    public ResponseEntity<BaseResponse<Void>> updateMyProfile(
            HttpServletResponse response, HttpServletRequest request,
            @ModelAttribute UpdateMyProfileRequest updateMyProfileRequest) throws IOException {

        Long userId = tokenUtil.findIdByToken(request);
        userService.updateMyProfile(userId, request, updateMyProfileRequest, response);

        return ResponseEntity.ok(BaseResponse.withMessage("프로필 수정 완료"));
    }


    @GetMapping("/point")
    public ResponseEntity<BaseResponse<GetUserPointResponse>> getUserPoint(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        GetUserPointResponse response = userService.getUserPoint(userId);

        return ResponseEntity.ok(BaseResponse.success("포인트 조회 성공", response));
    }


    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Map<String, Object>>> searchUser(
            @RequestParam String keyword, @RequestParam int pageNum) {

        Map<String, Object> response = userService.searchUser(keyword, pageNum);

        return ResponseEntity.ok(BaseResponse.success("유저 검색 성공", response));
    }


    @GetMapping("/reward")
    public ResponseEntity<BaseResponse<List<GetMyRewardRespond>>> getMyReward(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        List<GetMyRewardRespond> response = userService.getMyReward(userId);

        return ResponseEntity.ok(BaseResponse.success("리워드 조회 성공", response));
    }


    @GetMapping("/donation")
    public ResponseEntity<BaseResponse<List<GetUserDonation>>> getUserDonation(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        List<GetUserDonation> response = userService.getUserDonation(userId);

        return ResponseEntity.ok(BaseResponse.success("기부내역 조회 성공", response));
    }
}
