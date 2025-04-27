package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetSumPointRankResponse {

    private Long userId;
    private String nickname;
    private String profileImage;
    private int SumPoint;
    private int rank;
}
