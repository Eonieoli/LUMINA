package com.lumina.backend.common.exception;

import com.lumina.backend.common.model.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * CustomException 예외가 발생했을 때 처리하는 메서드
     * 클라이언트에게 에러 메시지를 포함한 응답 반환
     *
     * @param ex CustomException 발생한 사용자 정의 예외 객체
     * @return ResponseEntity<BaseResponse<Void>> 에러 메시지와 상태 코드를 포함한 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Void>> handleCustomException(
            CustomException ex) {

        return ResponseEntity.status(ex.getStatus())
                .body(BaseResponse.error(ex.getMessage()));
    }


    /**
     * 파일 업로드 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error("업로드 가능한 파일 크기는 최대 5MB입니다."));
    }
}
