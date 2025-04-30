package com.lumina.backend.lumina.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluateCommentResponse {

    private String comment_id;
    private String reply;
    private String respondedBy;
}
