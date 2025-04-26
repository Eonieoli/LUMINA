package com.lumina.backend.category.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCategoryResponse {

    private Long categoryId;
    private String categoryName;
    private Boolean isSubscribe;
}
