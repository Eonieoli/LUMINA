package com.lumina.backend.category.service;

import com.lumina.backend.category.model.response.GetCategoryResponse;

import java.util.List;

public interface CategoryService {

    List<GetCategoryResponse> getCategory(Long userId);

    Boolean toggleCategorySubscribe(Long userId, Long categoryId);
}
