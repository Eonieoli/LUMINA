package com.lumina.backend.common.utill;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.user.model.entity.User;
import org.springframework.http.HttpStatus;

public class ValidationUtil {

    // 유저 ID 유효성 검사
    public static void validateId(Long id, String idName) {
        if (id == null || id <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 " + idName + " ID입니다.");
        }
    }

    // 필수 입력값 검사
    public static void validateRequiredField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, fieldName + "은(는) 필수 입력값입니다.");
        }
    }

    public static void validateRequiredField(Long value, String fieldName) {
        if (value == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, fieldName + "은(는) 필수 입력값입니다.");
        }
    }

    public static void validateRequiredField(Integer value, String fieldName) {
        if (value == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, fieldName + "은(는) 필수 입력값입니다.");
        }
    }

    //페이지네이션 유효성 검사
    public static void validatePageNumber(int pageNum) {
        if (pageNum <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상이어야 합니다.");
        }
    }

    //팔로우 유효성 검사
    public static void validateFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "자신을 팔로우할 수 없습니다.");
        }
    }

    //게시물 삭제 권한 검사
    public static void validatePostDelete(String role, Post post, Long userId) {
        if (role.equals("ROLE_USER") && !post.getUser().getId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "사진 삭제 권한이 없습니다.");
        }
    }

    // 부모 댓글 검사
    public static void validateComment(Comment comment, Long postId) {

        if (!comment.getPost().getId().equals(postId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "해당 게시글의 댓글이 아닙니다");
        }
    }

    //게시물 삭제 권한 검사
    public static void validateCommentDelete(String role, Comment comment, Long userId) {
        if (role.equals("ROLE_USER") && !comment.getUser().getId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "사진 삭제 권한이 없습니다.");
        }
    }

    //보유 포인트 검사
    public static void validateUserPoint(User user, int point) {
        if (user.getPoint() < point) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "보유 point가 부족합니다.");
        }
    }
}
