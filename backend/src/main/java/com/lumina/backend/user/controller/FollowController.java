package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.user.model.request.ToggleFollowRequest;
import com.lumina.backend.user.service.FollowService;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowController {

    private final OAuthService oAuthService;
    private final FollowService followService;

    private final TokenUtil tokenUtil;


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

        Long followerId = tokenUtil.findIdByToken(request);
        Boolean follow = followService.toggleFollow(followerId, toggleFollowRequest.getFollowingId());

        BaseResponse<Void> baseResponse = follow ?
                BaseResponse.withMessage("팔로잉 추가 완료") :
                BaseResponse.withMessage("팔로잉 취소 완료");

        return ResponseEntity.ok(baseResponse);
    }


    /**
     * 현재 사용자의 팔로워 목록을 조회하는 엔드포인트
     *
     * @param request HTTP 요청 객체
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 팔로워 목록 정보
     */
    @GetMapping("/follower")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getFollowers(
            HttpServletRequest request,
            @RequestParam(required = false) Long userId,
            @RequestParam int pageNum) {

        Long myId = tokenUtil.findIdByToken(request);
        Long targetUserId = (userId != null) ? userId : myId;
        boolean isMe = (userId == null || userId.equals(myId));

        Map<String, Object> reponse = followService.getFollowers(myId, targetUserId, isMe, pageNum);

        return ResponseEntity.ok(BaseResponse.success("팔로워 조회 성공", reponse));
    }


    /**
     * 현재 사용자의 팔로워 목록을 조회하는 엔드포인트
     *
     * @param request HTTP 요청 객체
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 팔로워 목록 정보
     */
    @GetMapping("/following")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getFollowings(
            HttpServletRequest request,
            @RequestParam(required = false) Long userId,
            @RequestParam int pageNum) {

        Long myId = tokenUtil.findIdByToken(request);
        Long targetUserId = (userId != null) ? userId : myId;
        boolean isMe = (userId == null || userId.equals(myId));

        Map<String, Object> reponse = followService.getFollowings(myId, targetUserId, isMe, pageNum);

        return ResponseEntity.ok(BaseResponse.success("팔로잉 조회 성공", reponse));
    }


    /**
     * 현재 사용자의 팔로워를 삭제하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (현재 사용자 인증 정보 포함)
     * @param userId  삭제할 팔로워의 ID
     * @return ResponseEntity<BaseResponse < Void>> 삭제 결과 응답
     */
    @DeleteMapping("/follower/{userId}")
    public ResponseEntity<BaseResponse<Void>> deleteMyFollower(
            HttpServletRequest request,
            @PathVariable Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        followService.deleteMyFollower(myId, userId);

        return ResponseEntity.ok(BaseResponse.withMessage("팔로워 삭제 완료"));
    }
}
