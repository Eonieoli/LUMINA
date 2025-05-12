package com.lumina.backend.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetToken {

    private String access;
    private String refresh;
}
