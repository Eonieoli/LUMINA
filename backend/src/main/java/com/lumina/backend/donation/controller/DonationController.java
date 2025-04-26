package com.lumina.backend.donation.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.donation.service.DonationService;
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
@RequestMapping("/api/v1/donation")
@RequiredArgsConstructor
public class DonationController {

    private final OAuthService oAuthService;
    private final DonationService donationService;


    @GetMapping("")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getDonation(
            HttpServletRequest request, @RequestParam int pageNum) {

        Long userId = oAuthService.findIdByToken(request);
        Map<String, Object> response = donationService.getDonation(userId, pageNum);

        return ResponseEntity.ok(BaseResponse.success("전체 기부처 조회 성공", response));
    }
}
