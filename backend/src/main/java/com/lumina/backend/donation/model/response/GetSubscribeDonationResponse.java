package com.lumina.backend.donation.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetSubscribeDonationResponse {

    private Long donationId;
    private String donationName;
}
