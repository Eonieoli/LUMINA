package com.lumina.backend.user.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.user.model.response.GetSumPointRankResponse;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class BoardController {

    private final UserService userService;

    private final TokenUtil tokenUtil;


    @GetMapping("/rank")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getSumPointRank(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        Map<String, Object> response = userService.getSumPointRank(userId);

        return ResponseEntity.ok(BaseResponse.success("누적 기부금 수치 랭킹 조회 성공", response));
    }
}
