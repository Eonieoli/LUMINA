package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserPointResponse {

    private Long userId;
    private String nickname;
    private Integer point;
}
