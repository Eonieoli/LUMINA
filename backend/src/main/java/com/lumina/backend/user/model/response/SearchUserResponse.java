package com.lumina.backend.user.model.response;

import com.lumina.backend.user.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchUserResponse {

    private Long userId;
    private String profileImage;
    private String nickname;

    public static SearchUserResponse from(User user) {
        return new SearchUserResponse(user.getId(), user.getProfileImage(), user.getNickname());
    }
}
