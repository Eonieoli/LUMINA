package com.lumina.backend.post.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.GetPostResponse;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
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


    /**
     * 특정 게시물을 삭제하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (사용자 인증 정보 포함)
     * @param postId 삭제할 게시물 ID
     * @return ResponseEntity<BaseResponse<Void>> 삭제 결과 응답
     */
    @DeleteMapping("{postId}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            HttpServletRequest request, @PathVariable Long postId) {

        Long userId = oAuthService.findIdByToken(request);
        String role = oAuthService.findRoleByToken(request);
        postService.deletePost(userId, role, postId);

        return ResponseEntity.ok(BaseResponse.withMessage("게시물 삭제 완료"));
    }


    /**
     * 게시물 좋아요를 토글하는 API
     *
     * @param request   사용자 인증 정보를 포함한 HTTP 요청 객체
     * @param postId   좋아요를 토글할 게시물의 ID
     * @return ResponseEntity<BaseResponse<Void>> 좋아요 상태에 따른 응답 메시지 반환
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<BaseResponse<Void>> toggleLike(
            HttpServletRequest request, @PathVariable Long postId) {

        Long userId = oAuthService.findIdByToken(request);
        Boolean like = postService.toggleLike(userId, postId);

        // 결과에 따른 응답 메시지 생성
        BaseResponse<Void> baseResponse = like ?
                BaseResponse.withMessage("게시물 좋아요 완료") :
                BaseResponse.withMessage("게시물 좋아요 취소 완료");

        // 응답 반환
        return ResponseEntity.ok(baseResponse);
    }


    @PostMapping("/{postId}/comment")
    public ResponseEntity<BaseResponse<Void>> uploadComment(
            HttpServletRequest request,
            @RequestBody UploadCommentRequest uploadCommentRequest,
            @PathVariable Long postId) {

        Long userId = oAuthService.findIdByToken(request);
        postService.uploadComment(userId, postId, uploadCommentRequest);

        return ResponseEntity.ok(BaseResponse.withMessage("댓글 등록 완료"));
    }


    @GetMapping("/{postId}/comment")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getComment(
            HttpServletRequest request,
            @PathVariable Long postId,
            @RequestParam int pageNum) {

        Long userId = oAuthService.findIdByToken(request);
        Map<String, Object> response = postService.getComment(userId, postId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("댓글 조회 성공", response));
    }


    @GetMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getChildComment(
            HttpServletRequest request,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam int pageNum) {

        Long userId = oAuthService.findIdByToken(request);
        Map<String, Object> response = postService.getChildComment(userId, postId, commentId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("대댓글 조회 성공", response));
    }
}
