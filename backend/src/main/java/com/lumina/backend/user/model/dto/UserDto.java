package com.lumina.backend.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private String socialId;
    private String nickname;
    private String role;
}
