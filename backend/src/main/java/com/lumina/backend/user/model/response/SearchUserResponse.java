package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchUserResponse {

    private Long userId;
    private String profileImage;
    private String nickname;
}
