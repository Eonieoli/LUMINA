package com.lumina.backend.admin.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserResponse {

    private Long userId;
    private String nickname;
    private String profileImage;
    private String message;
    private int point;
    private int sumPoint;
    private int grade;
    private int positiveness;
}
