package com.lumina.backend.post.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.GetPostResponse;
import com.lumina.backend.post.model.response.UploadCommentResponse;
import com.lumina.backend.post.model.response.UploadPostResponse;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final TokenUtil tokenUtil;


    @PostMapping("")
    public ResponseEntity<BaseResponse<UploadPostResponse>> uploadPost(
            HttpServletRequest request,
            @ModelAttribute UploadPostRequest uploadPostRequest) throws IOException {

        Long userId = tokenUtil.findIdByToken(request);
        UploadPostResponse response = postService.uploadPost(userId, uploadPostRequest);

        return ResponseEntity.ok(BaseResponse.success("게시물 등록 완료", response));
    }


    @GetMapping("")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getPost(
            HttpServletRequest request, @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String categoryName, @RequestParam int pageNum) {

        Long myId = tokenUtil.findIdByToken(request);
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

        Long userId = tokenUtil.findIdByToken(request);
        String role = tokenUtil.findRoleByToken(request);
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

        Long userId = tokenUtil.findIdByToken(request);
        Boolean like = postService.toggleLike(userId, postId);

        BaseResponse<Void> baseResponse = like ?
                BaseResponse.withMessage("게시물 좋아요 완료") :
                BaseResponse.withMessage("게시물 좋아요 취소 완료");

        return ResponseEntity.ok(baseResponse);
    }


    @GetMapping("/category")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getSubscribePost(
            HttpServletRequest request, @RequestParam int pageNum) {

        Long userId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = postService.getSubscribePost(userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("구독 카테고리 게시물 조회 성공", response));
    }


    /**
     * 게시물를 검색하는 엔드포인트
     *
     * @param keyword 검색어 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색 결과 응답
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Map<String, Object>>> searchPost(
            HttpServletRequest request, @RequestParam String keyword,
            @RequestParam int pageNum) {

        Long userId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = postService.searchPost(userId, keyword, pageNum);

        return ResponseEntity.ok(BaseResponse.success("게시물 검색 성공", response));
    }
}
