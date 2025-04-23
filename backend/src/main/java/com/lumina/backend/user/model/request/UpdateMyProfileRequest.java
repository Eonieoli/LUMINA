package com.lumina.backend.user.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class UpdateMyProfileRequest {

    private MultipartFile profileImageFile;
    private String nickname;
    private String message;
}
