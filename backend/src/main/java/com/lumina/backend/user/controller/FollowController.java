package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.user.model.request.ToggleFollowRequest;
import com.lumina.backend.user.model.response.GetFollowsResponse;
import com.lumina.backend.user.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    private final TokenUtil tokenUtil;


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


    @GetMapping("/follower")
    public ResponseEntity<BaseResponse<List<GetFollowsResponse>>> getFollowers(
            HttpServletRequest request, @RequestParam(required = false) Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        Long targetUserId = (userId != null) ? userId : myId;
        List<GetFollowsResponse> response = followService.getFollowers(myId, targetUserId);

        return ResponseEntity.ok(BaseResponse.success("팔로워 조회 성공", response));
    }


    @GetMapping("/following")
    public ResponseEntity<BaseResponse<List<GetFollowsResponse>>> getFollowings(
            HttpServletRequest request, @RequestParam(required = false) Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        Long targetUserId = (userId != null) ? userId : myId;
        List<GetFollowsResponse> response = followService.getFollowings(myId, targetUserId);

        return ResponseEntity.ok(BaseResponse.success("팔로잉 조회 성공", response));
    }


    @DeleteMapping("/follower/{userId}")
    public ResponseEntity<BaseResponse<Void>> deleteMyFollower(
            HttpServletRequest request, @PathVariable Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        followService.deleteMyFollower(myId, userId);

        return ResponseEntity.ok(BaseResponse.withMessage("팔로워 삭제 완료"));
    }
}
