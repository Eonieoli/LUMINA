package com.lumina.backend.donation.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.donation.model.response.GetSubscribeDonationResponse;
import com.lumina.backend.donation.service.DonationService;
import com.lumina.backend.donation.model.request.DoDonationRequest;
import com.lumina.backend.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PostMapping("")
    public ResponseEntity<BaseResponse<Void>> doDonation(
            HttpServletRequest request,
            @RequestBody DoDonationRequest doDonationRequest) {

        Long userId = oAuthService.findIdByToken(request);
        donationService.doDonation(userId, doDonationRequest);

        return ResponseEntity.ok(BaseResponse.withMessage("기부 완료"));
    }


    /**
     * 기부처 구독을 토글하는 API
     *
     * @param request   사용자 인증 정보를 포함한 HTTP 요청 객체
     * @param donationId   구독을 토글할 기부처의 ID
     * @return ResponseEntity<BaseResponse<Void>> 구독 상태에 따른 응답 메시지 반환
     */
    @PostMapping("/{donationId}")
    public ResponseEntity<BaseResponse<Void>> toggleDonationSubscribe(
            HttpServletRequest request, @PathVariable Long donationId) {

        Long userId = oAuthService.findIdByToken(request);
        Boolean subscribe = donationService.toggleDonationSubscribe(userId, donationId);

        // 결과에 따른 응답 메시지 생성
        BaseResponse<Void> baseResponse = subscribe ?
                BaseResponse.withMessage("기부처 구독 완료") :
                BaseResponse.withMessage("기부처 구독 취소 완료");

        // 응답 반환
        return ResponseEntity.ok(baseResponse);
    }


    @GetMapping("/me")
    public ResponseEntity<BaseResponse<List<GetSubscribeDonationResponse>>> getSubscribeDonation(
            HttpServletRequest request) {

        Long userId = oAuthService.findIdByToken(request);
        List<GetSubscribeDonationResponse> response = donationService.getSubscribeDonation(userId);

        return ResponseEntity.ok(BaseResponse.success("관심 기부처 조회 성공", response));
    }


    /**
     * 기부처를 검색하는 엔드포인트
     *
     * @param keyword 검색어 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색 결과 응답
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Map<String, Object>>> searchDonation(
            @RequestParam String keyword,
            @RequestParam int pageNum) {

        Map<String, Object> response = donationService.searchDonation(keyword, pageNum);

        return ResponseEntity.ok(BaseResponse.success("기부처 검색 성공", response));
    }
}
