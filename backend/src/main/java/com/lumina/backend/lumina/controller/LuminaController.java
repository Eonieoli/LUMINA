package com.lumina.backend.lumina.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.lumina.service.LuminaService;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.service.CommentService;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lumina")
@RequiredArgsConstructor
public class LuminaController {

    private final UserRepository userRepository;

    private final LuminaService luminaService;
    private final CommentService commentService;

    private final TokenUtil tokenUtil;


    @PostMapping("/post/{postId}")
    public ResponseEntity<BaseResponse<Void>> getPostLumina(
            HttpServletRequest request,
            @PathVariable Long postId) {

        Long userId = tokenUtil.findIdByToken(request);
        Long luminaId = userRepository.findIdByNickname("Luna");
        UploadCommentRequest uploadCommentRequest = luminaService.getPostLumina(userId, postId);
        commentService.uploadComment(luminaId, postId, uploadCommentRequest);

        return ResponseEntity.ok(BaseResponse.withMessage("게시물에 대한 Lumina 댓글 성성 완료"));
    }


    @PostMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<BaseResponse<Void>> getCommentLumina(
            HttpServletRequest request,
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        Long userId = tokenUtil.findIdByToken(request);
        Long luminaId = userRepository.findIdByNickname("Luna");
        UploadCommentRequest uploadCommentRequest = luminaService.getCommentLumina(userId, commentId);
        commentService.uploadComment(luminaId, postId, uploadCommentRequest);

        return ResponseEntity.ok(BaseResponse.withMessage("댓글에 대한 Lumina 댓글 성성 완료"));
    }
}
