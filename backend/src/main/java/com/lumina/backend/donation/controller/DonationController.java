package com.lumina.backend.donation.controller;

import com.lumina.backend.common.model.response.BaseResponse;
import com.lumina.backend.common.utill.TokenUtil;
import com.lumina.backend.donation.model.response.GetDetailDonationResponse;
import com.lumina.backend.donation.model.response.GetDonationResponse;
import com.lumina.backend.donation.service.DonationService;
import com.lumina.backend.donation.model.request.DoDonationRequest;
import com.lumina.backend.lumina.service.LuminaService;
import com.lumina.backend.user.repository.UserRepository;
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

    private final UserRepository userRepository;

    private final DonationService donationService;
    private final LuminaService luminaService;

    private final TokenUtil tokenUtil;


    @GetMapping("")
    public ResponseEntity<BaseResponse<List<GetDonationResponse>>> getDonation(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        List<GetDonationResponse> response = donationService.getDonation(userId);

        return ResponseEntity.ok(BaseResponse.success("전체 기부처 조회 성공", response));
    }


    @PostMapping("")
    public ResponseEntity<BaseResponse<Void>> doDonation(
            HttpServletRequest request, @RequestBody DoDonationRequest doDonationRequest) {

        Long userId = tokenUtil.findIdByToken(request);
        donationService.doDonation(userId, doDonationRequest);

        return ResponseEntity.ok(BaseResponse.withMessage("기부 완료"));
    }


    @PostMapping("/{donationId}")
    public ResponseEntity<BaseResponse<Void>> toggleDonationSubscribe(
            HttpServletRequest request, @PathVariable Long donationId) {

        Long userId = tokenUtil.findIdByToken(request);
        Boolean subscribe = donationService.toggleDonationSubscribe(userId, donationId);

        BaseResponse<Void> baseResponse = subscribe ?
                BaseResponse.withMessage("기부처 구독 완료") :
                BaseResponse.withMessage("기부처 구독 취소 완료");

        return ResponseEntity.ok(baseResponse);
    }


    @GetMapping("/me")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getSubscribeDonation(
            HttpServletRequest request) {

        Long userId = tokenUtil.findIdByToken(request);
        if (userRepository.findLikeCntByUserId(userId) >= 20) {
            luminaService.getAiDonation(userId);
        }
        Map<String, Object> response = donationService.getSubscribeDonation(userId);

        return ResponseEntity.ok(BaseResponse.success("관심 기부처 조회 성공", response));
    }


    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Map<String, Object>>> searchDonation(
            @RequestParam String keyword, @RequestParam int pageNum) {

        Map<String, Object> response = donationService.searchDonation(keyword, pageNum);

        return ResponseEntity.ok(BaseResponse.success("기부처 검색 성공", response));
    }


    @GetMapping("{donationId}")
    public ResponseEntity<BaseResponse<GetDetailDonationResponse>> getDetailDonation(
            HttpServletRequest request, @PathVariable Long donationId) {

        Long userId = tokenUtil.findIdByToken(request);
        GetDetailDonationResponse response = donationService.getDetailDonation(userId, donationId);

        return ResponseEntity.ok(BaseResponse.success("전체 기부처 조회 성공", response));
    }
}
