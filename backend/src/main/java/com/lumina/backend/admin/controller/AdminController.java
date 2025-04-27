package com.lumina.backend.admin.controller;

import com.lumina.backend.admin.model.response.GetUserResponse;
import com.lumina.backend.admin.service.AdminService;
import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OAuthService oAuthService;


    @GetMapping("/user")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUser(
            HttpServletRequest request,
            @RequestParam int pageNum) {

        Long userId = oAuthService.findIdByToken(request);

        Map<String, Object> response = adminService.getUser(userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("전체 유저 조회 완료", response));
    }
}
