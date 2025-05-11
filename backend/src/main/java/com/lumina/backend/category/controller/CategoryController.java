package com.lumina.backend.category.controller;

import com.lumina.backend.category.model.response.GetCategoryResponse;
import com.lumina.backend.category.service.CategoryService;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final TokenUtil tokenUtil;

    private final CategoryService categoryService;


    @GetMapping("")
    public ResponseEntity<BaseResponse<List<GetCategoryResponse>>> getCategory(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        List<GetCategoryResponse> response = categoryService.getCategory(userId);

        return ResponseEntity.ok(BaseResponse.success("전체 카테고리 조회 성공", response));
    }


    @PostMapping("/{categoryId}")
    public ResponseEntity<BaseResponse<Void>> toggleCategorySubscribe(
            HttpServletRequest request, @PathVariable Long categoryId) {

        Long userId = tokenUtil.findIdByToken(request);
        Boolean subscribe = categoryService.toggleCategorySubscribe(userId, categoryId);

        BaseResponse<Void> baseResponse = subscribe ?
                BaseResponse.withMessage("카테고리 구독 완료") :
                BaseResponse.withMessage("카테고리 구독 취소 완료");

        return ResponseEntity.ok(baseResponse);
    }
}
