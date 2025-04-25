package com.lumina.backend.post.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.GetPostResponse;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final OAuthService oAuthService;
    private final PostService postService;

    @PostMapping("")
    public ResponseEntity<BaseResponse<Void>> uploadPost(
            HttpServletRequest request,
            @ModelAttribute UploadPostRequest uploadPostRequest) throws IOException {

        Long userId = oAuthService.findIdByToken(request);
        postService.uploadPost(userId, uploadPostRequest);

        return ResponseEntity.ok(BaseResponse.withMessage("게시물 등록 완료"));
    }


    @GetMapping("")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getPost(
            HttpServletRequest request,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String categoryName,
            @RequestParam int pageNum) {

        Long myId = oAuthService.findIdByToken(request);
        Map<String, Object> response = postService.getPosts(myId, userId, categoryName, pageNum);

        return ResponseEntity.ok(BaseResponse.success("게시물 조회 성공", response));
    }


    @DeleteMapping("{postId}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            HttpServletRequest request, @PathVariable Long postId) {

        Long userId = oAuthService.findIdByToken(request);
        String role = oAuthService.findRoleByToken(request);
        postService.deletePost(userId, role, postId);

        return ResponseEntity.ok(BaseResponse.withMessage("게시물 삭제 완료"));
    }
}
