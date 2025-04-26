package com.lumina.backend.category.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.model.response.GetCategoryResponse;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;


    @Override
    public List<GetCategoryResponse> getCategory(Long userId) {

        List<Category> categories = categoryRepository.findAll();

        List<GetCategoryResponse> categoryList = categories.stream()
                .map(category -> {
                    Boolean isSubscribe = userCategoryRepository.existsByUserIdAndCategoryId(userId, category.getId());
                    return new GetCategoryResponse(
                            category.getId(), category.getCategoryName(), isSubscribe
                    );
                })
                .collect(Collectors.toList());

        return  categoryList;
    }
}
