package com.lumina.backend.post.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

}
