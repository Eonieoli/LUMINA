package com.lumina.backend.post.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
public class UploadPostRequest {

    private MultipartFile postImageFile;
    private List<String> hashtag;
    private String postContent;
}
