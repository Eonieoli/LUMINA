package com.lumina.backend.admin.controller;

import com.lumina.backend.admin.service.AdminService;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final TokenUtil tokenUtil;


    @GetMapping("/user")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUser(
            HttpServletRequest request, @RequestParam int pageNum) {

        Long userId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = adminService.getUser(userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("전체 유저 조회 완료", response));
    }


    @DeleteMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<Void>> deleteUser(
            HttpServletRequest request, @PathVariable Long userId) {

        Long myId = tokenUtil.findIdByToken(request);
        adminService.deleteUser(myId, userId);

        return ResponseEntity.ok(BaseResponse.withMessage("유저 삭제 완료"));
    }


    @GetMapping("/cur-user")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getCurUser(
            HttpServletRequest request, @RequestParam int pageNum) {

        Long userId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = adminService.getCurUser(userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("현재 접속 유저 조회 완료", response));
    }


    @GetMapping("/post")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUserPost(
            HttpServletRequest request, @RequestParam Long userId,
            @RequestParam int pageNum) {

        Long myId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = adminService.getUserPost(myId, userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("유저 게시물 조회 완료", response));
    }


    @GetMapping("/comment")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUserComment(
            HttpServletRequest request, @RequestParam Long userId,
            @RequestParam int pageNum) {

        Long myId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = adminService.getUserComment(myId, userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("유저 댓글 조회 완료", response));
    }


    @DeleteMapping("/post/{postId}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            HttpServletRequest request, @PathVariable Long postId) {

        Long myId = tokenUtil.findIdByToken(request);
        adminService.deletePost(myId, postId);

        return ResponseEntity.ok(BaseResponse.withMessage("게시물 삭제 완료"));
    }


    @DeleteMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            HttpServletRequest request, @PathVariable Long postId,
            @PathVariable Long commentId) {

        Long myId = tokenUtil.findIdByToken(request);
        adminService.deleteComment(myId, postId, commentId);

        return ResponseEntity.ok(BaseResponse.withMessage("댓글 삭제 완료"));
    }
}
