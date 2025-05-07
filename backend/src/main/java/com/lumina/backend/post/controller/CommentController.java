package com.lumina.backend.post.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.post.model.request.UploadCommentRequest;
import com.lumina.backend.post.model.response.GetChildCommentResponse;
import com.lumina.backend.post.model.response.UploadCommentResponse;
import com.lumina.backend.post.service.CommentService;
import com.lumina.backend.post.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/post/{postId}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final TokenUtil tokenUtil;


    @PostMapping("")
    public ResponseEntity<BaseResponse<UploadCommentResponse>> uploadComment(
            HttpServletRequest request, @RequestBody UploadCommentRequest uploadCommentRequest,
            @PathVariable Long postId) {

        Long userId = tokenUtil.findIdByToken(request);
        UploadCommentResponse response = commentService.uploadComment(userId, postId, uploadCommentRequest);

        return ResponseEntity.ok(BaseResponse.success("댓글 등록 완료", response));
    }


    @GetMapping("")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getComment(
            HttpServletRequest request, @PathVariable Long postId,
            @RequestParam int pageNum) {

        Long userId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = commentService.getComment(userId, postId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("댓글 조회 성공", response));
    }


    @GetMapping("/{commentId}")
    public ResponseEntity<BaseResponse<List<GetChildCommentResponse>>> getChildComment(
            HttpServletRequest request, @PathVariable Long postId,
            @PathVariable Long commentId) {

        Long userId = tokenUtil.findIdByToken(request);
        List<GetChildCommentResponse> response = commentService.getChildComment(userId, postId, commentId);

        return ResponseEntity.ok(BaseResponse.success("대댓글 조회 성공", response));
    }


    /**
     * 특정 댓글을 삭제하는 엔드포인트
     *
     * @param request HTTP 요청 객체 (사용자 인증 정보 포함)
     * @param postId 댓글 게시물의 ID
     * @param commentId 삭제할 댓글의 ID
     * @return ResponseEntity<BaseResponse<Void>> 삭제 결과 응답
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            HttpServletRequest request, @PathVariable Long postId,
            @PathVariable Long commentId) {

        Long userId = tokenUtil.findIdByToken(request);
        String role = tokenUtil.findRoleByToken(request);
        commentService.deleteComment(userId, role, postId, commentId);

        return ResponseEntity.ok(BaseResponse.withMessage("댓글 삭제 완료"));
    }


    /**
     * 댓글 좋아요를 토글하는 API
     *
     * @param request    사용자 인증 정보를 포함한 HTTP 요청 객체
     * @param postId     댓글 게시물의 ID
     * @param commentId  좋아요 할 댓글의 ID
     * @return ResponseEntity<BaseResponse<Void>> 좋아요 상태에 따른 응답 메시지 반환
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<BaseResponse<Void>> toggleCommentLike(
            HttpServletRequest request, @PathVariable Long postId,
            @PathVariable Long commentId) {

        Long userId = tokenUtil.findIdByToken(request);
        Boolean like = commentService.toggleCommentLike(userId, postId, commentId);

        BaseResponse<Void> baseResponse = like ?
                BaseResponse.withMessage("댓글 좋아요 완료") :
                BaseResponse.withMessage("댓글 좋아요 취소 완료");

        return ResponseEntity.ok(baseResponse);
    }
}
