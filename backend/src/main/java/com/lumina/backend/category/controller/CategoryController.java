package com.lumina.backend.category.controller;

import com.lumina.backend.category.model.response.GetCategoryResponse;
import com.lumina.backend.category.service.CategoryService;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final OAuthService oAuthService;
    private final CategoryService categoryService;


    @GetMapping("")
    public ResponseEntity<BaseResponse<List<GetCategoryResponse>>> getCategory(
            HttpServletRequest request) {

        Long userId = oAuthService.findIdByToken(request);
        List<GetCategoryResponse> response = categoryService.getCategory(userId);

        return ResponseEntity.ok(BaseResponse.success("전체 카테고리 조회 성공", response));
    }
}
