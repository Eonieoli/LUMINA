package com.lumina.backend.user.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoDonationRequest {

    private String donationName;
    private Integer point;
}
