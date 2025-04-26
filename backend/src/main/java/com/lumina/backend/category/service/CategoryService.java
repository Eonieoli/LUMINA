package com.lumina.backend.category.service;

import com.lumina.backend.category.model.response.GetCategoryResponse;

import java.util.List;

public interface CategoryService {

    List<GetCategoryResponse> getCategory(Long userId);

    /**
     * 카테고리에 대한 구독 토글 메서드
     */
    Boolean toggleSubscribe(Long userId, Long categoryId);
}
