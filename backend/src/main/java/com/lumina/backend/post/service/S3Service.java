package com.lumina.backend.post.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 사진 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface S3Service {

    /**
     * 프로필 이미지를 S3에 업로드하는 메서드
     */
    String uploadImageFile(MultipartFile file, String folderName) throws IOException;

    /**
     * S3에서 프로필 이미지 삭제
     */
    void deleteImageFile(String imageUrl, String folderName);

    /**
     * permanent 폴더 파일명 추출
     */
    String extractFileName(String url, String folderName);

    /**
     * S3 파일 URL 생성
     */
    String getFileUrl(String folderName, String fileName);
}
