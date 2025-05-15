package com.lumina.backend.lumina.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetPostRequest {

    private String postContent;
}
