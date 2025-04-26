package com.lumina.backend.category.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.model.entity.UserCategory;
import com.lumina.backend.category.model.response.GetCategoryResponse;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.category.service.CategoryService;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostLike;
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
    private final UserRepository userRepository;


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

        if (categoryId == null || categoryId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리 ID입니다.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다. 카테고리 ID: " + categoryId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        UserCategory existUserCategory = userCategoryRepository.findByUserIdAndCategoryId(userId, categoryId)
                .orElse(null);

        if (existUserCategory != null) {
            // 기존 구독 관계가 있으면 구독 취소
            userCategoryRepository.delete(existUserCategory);
            return false;
        } else {
            UserCategory userCategory = new UserCategory(user, category);
            userCategoryRepository.save(userCategory);
        }
        return true;
    }
}
