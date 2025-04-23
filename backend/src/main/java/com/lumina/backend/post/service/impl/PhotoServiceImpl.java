package com.lumina.backend.post.service.impl;

import com.lumina.backend.post.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;
}
