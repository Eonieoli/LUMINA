package com.lumina.backend.donation.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetDetailDonationResponse {

    private Long donationId;
    private String donationName;
    private int sumPoint;
    private int sumUser;
    private int myDonationCnt;
    private int mySumDonation;
    private Boolean isSubscribe;
}
