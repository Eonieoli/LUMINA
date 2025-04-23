package com.lumina.backend.post.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;


    /**
     * 이미지 파일을 S3에 업로드하는 메서드
     *
     * @param file 업로드할 MultipartFile 객체
     * @param folderName 업로드할 폴더 Name
     * @return String 업로드된 파일의 URL
     * @throws CustomException 파일 처리 중 발생할 수 있는 입출력 예외
     */
    @Override
    public String uploadImageFile(MultipartFile file, String folderName) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "업로드 파일이 유효하지 않습니다");
        }

        String fileName = UUID.randomUUID() + "." + getFileExtension(file.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folderName + "/" + fileName)
                .contentType(file.getContentType())
                .build();

        try {
            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 파일의 URL 반환
            return getFileUrl(folderName + "/", fileName);
        } catch (Exception e) {
            // 업로드 실패 시 에러 로그 출력 및 예외 발생
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패: " + e);
        }
    }


    /**
     * S3 버킷 내 특정 폴더와 파일명을 기반으로 파일 URL을 생성하는 메서드
     *
     * @param folder 폴더 이름 (예: "profile/", "permanent/")
     * @param fileName 파일 이름
     * @return String 생성된 파일 URL
     */
    @Override
    public String getFileUrl(
            String folder, String fileName) {

        return String.format("https://%s.s3.%s.amazonaws.com/%s%s",
                bucketName,
                s3Client.serviceClientConfiguration().region(),
                folder,
                fileName);
    }


    /**
     * 파일명에서 확장자를 추출하는 메서드
     *
     * @param originalFileName 원본 파일명
     * @return String 파일 확장자 (점 포함)
     */
    private String getFileExtension(
            String originalFileName) {

        // 1. null 또는 빈 문자열 체크
        if (originalFileName == null || originalFileName.isEmpty()) {
            return "";
        }

        // 2. 마지막 점(.) 위치 찾기
        int extensionIndex = originalFileName.lastIndexOf('.');

        // 3. 유효성 검사 (점이 없거나, 첫 문자에 있을 경우 제외)
        if (extensionIndex <= 0 || extensionIndex >= originalFileName.length() - 1) {
            return "";
        }

        // 4. 소문자로 통일하여 반환 (선택사항)
        return originalFileName.substring(extensionIndex);
    }
}
