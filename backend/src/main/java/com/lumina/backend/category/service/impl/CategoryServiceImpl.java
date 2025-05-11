package com.lumina.backend.category.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.model.entity.UserCategory;
import com.lumina.backend.category.model.response.GetCategoryResponse;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.category.service.CategoryService;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
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


    /**
     * 전체 카테고리 목록을 조회합니다.
     * 각 카테고리에 대해 사용자의 구독 여부도 함께 반환합니다.
     *
     * @param userId 사용자 ID
     * @return List<GetCategoryResponse> 카테고리 응답 리스트
     */
    @Override
    public List<GetCategoryResponse> getCategory(Long userId) {

        return categoryRepository.findAll().stream()
                .map(category -> convertToCategoryResponse(category, userId))
                .collect(Collectors.toList());
    }


    /**
     * 카테고리 구독을 토글합니다.
     * 이미 구독 중이면 구독 취소, 아니면 구독 등록을 처리합니다.
     *
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID
     * @return Boolean 구독 상태 (true: 구독, false: 구독 취소)
     */
    @Override
    @Transactional
    public Boolean toggleCategorySubscribe(Long userId, Long categoryId) {

        ValidationUtil.validateId(categoryId, "카테고리");

        Category category = findUtil.getCategoryById(categoryId);
        User user = findUtil.getUserById(userId);

        // 이미 구독 중이면 구독 취소, 아니면 구독 등록
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


    /**
     * 카테고리 정보를 응답 객체로 변환합니다.
     *
     * @param category Category 엔티티
     * @param userId 사용자 ID
     * @return GetCategoryResponse 변환된 응답 객체
     */
    private GetCategoryResponse convertToCategoryResponse(Category category, Long userId) {

        Boolean isSubscribe = userCategoryRepository.existsByUserIdAndCategoryId(userId, category.getId());
        return new GetCategoryResponse(
                category.getId(), category.getCategoryName(), isSubscribe
        );
    }
}
