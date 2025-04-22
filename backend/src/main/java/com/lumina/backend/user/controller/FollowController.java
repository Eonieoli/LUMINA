package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.user.model.request.ToggleFollowRequest;
import com.lumina.backend.user.service.FollowService;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowController {

    private final OAuthService oAuthService;
    private final FollowService followService;


    /**
     * 현재 사용자의 팔로우 상태를 토글하는 엔드포인트
     *
     * @param toggleFollowRequest 팔로우 대상 사용자의 ID를 포함한 요청 객체
     * @param request             HTTP 요청 객체
     * @return ResponseEntity<BaseResponse < Void>> 팔로우 상태 변경 결과 메시지
     */
    @PostMapping("/following")
    public ResponseEntity<BaseResponse<Void>> toggleFollow(
            @RequestBody ToggleFollowRequest toggleFollowRequest,
            HttpServletRequest request) {

        Long followerId = oAuthService.findIdByToken(request);
        Boolean follow = followService.toggleFollow(followerId, toggleFollowRequest.getFollowingId());

        BaseResponse<Void> baseResponse = follow ?
                BaseResponse.withMessage("팔로잉 추가 완료") :
                BaseResponse.withMessage("팔로잉 취소 완료");

        return ResponseEntity.ok(baseResponse);
    }

}
