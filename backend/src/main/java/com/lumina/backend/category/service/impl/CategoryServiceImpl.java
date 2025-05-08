package com.lumina.backend.category.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.model.entity.UserCategory;
import com.lumina.backend.category.model.response.GetCategoryResponse;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.category.service.CategoryService;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostLike;
import com.lumina.backend.post.model.response.GetPostResponse;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;

    private final FindUtil findUtil;


    @Override
    public List<GetCategoryResponse> getCategory(Long userId) {

        return categoryRepository.findAll().stream()
                .map(category -> convertToCategoryResponse(category, userId))
                .collect(Collectors.toList());
    }


    /**
     * 카테고리 구독을 토글하는 API
     *
     * @param userId  사용자 ID
     * @param categoryId   구독을 토글할 카테고리의 ID
     * @return 구독 상태 (true: 구독, false: 구독 취소)
     */
    @Override
    @Transactional
    public Boolean toggleCategorySubscribe(Long userId, Long categoryId) {

        ValidationUtil.validateId(categoryId, "카테고리");

        Category category = findUtil.getCategoryById(categoryId);
        User user = findUtil.getUserById(userId);

        return userCategoryRepository.findByUserIdAndCategoryId(userId, categoryId)
                .map(existUserCategory -> {
                    userCategoryRepository.delete(existUserCategory);
                    return false;
                })
                .orElseGet(() -> {
                    UserCategory userCategory = new UserCategory(user, category);
                    userCategoryRepository.save(userCategory);
                    return true;
                });
    }


    private GetCategoryResponse convertToCategoryResponse(Category category, Long userId) {
        Boolean isSubscribe = userCategoryRepository.existsByUserIdAndCategoryId(userId, category.getId());
        return new GetCategoryResponse(
                category.getId(), category.getCategoryName(), isSubscribe
        );
    }
}
