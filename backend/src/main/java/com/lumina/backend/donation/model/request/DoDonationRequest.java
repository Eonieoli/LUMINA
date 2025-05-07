package com.lumina.backend.donation.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoDonationRequest {

    private Long donationId;
    private Integer point;
}
