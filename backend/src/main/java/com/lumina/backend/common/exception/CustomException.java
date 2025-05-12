package com.lumina.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;


    /**
     * 상태 코드와 메시지를 받아 사용자 정의 예외 객체 생성
     *
     * @param status 발생시킬 HTTP 상태 코드
     * @param message 예외 메시지
     */
    public CustomException(HttpStatus status, String message) {

        super(message);
        this.status = status;
    }
}

