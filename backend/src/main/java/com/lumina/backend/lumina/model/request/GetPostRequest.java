package com.lumina.backend.lumina.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class GetPostRequest {

    private MultipartFile post_image;
    private String postContent;
}
