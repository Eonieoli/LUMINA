package com.lumina.backend.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GetUserDonation {

    private Long donationId;
    private String donationName;
    private int donationCnt;
    private int donationPoint;
    private LocalDateTime createdAt;
}
