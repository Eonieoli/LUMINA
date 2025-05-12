package com.lumina.backend.lumina.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluatePostResponse {

    private String post_id;
    private String reward;
    private String evaluatedBy;
}