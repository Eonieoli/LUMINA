package com.lumina.backend.common.utill;

import com.lumina.backend.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ValidationUtil {

    // 유저 ID 유효성 검사
    public static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID입니다.");
        }
    }

    // 필수 입력값 검사
    public static void validateRequiredField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
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
}
