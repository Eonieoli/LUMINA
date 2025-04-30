package com.lumina.backend.donation.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.model.entity.UserDonation;
import com.lumina.backend.donation.model.response.GetDonationResponse;
import com.lumina.backend.donation.model.response.GetSubscribeDonationResponse;
import com.lumina.backend.donation.model.response.SearchDonationResponse;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.donation.repository.UserDonationRepository;
import com.lumina.backend.donation.service.DonationService;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.donation.model.request.DoDonationRequest;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final UserDonationRepository userDonationRepository;
    private final UserRepository userRepository;

    private final RedisUtil redisUtil;


    @Override
    public Map<String, Object> getDonation(
            Long userId, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Donation> donationPage = donationRepository.findByStatusTrue(pageRequest);

        List<GetDonationResponse> donationList = donationPage.getContent().stream()
                .map(donation -> {
                    Boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                            userId, donation.getId(), "USER");
                    return new GetDonationResponse(
                            donation.getId(), donation.getDonationName(), isSubscribe
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", donationPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("donations", donationList);

        return result;
    }


    @Override
    @Transactional
    public void doDonation(
            Long userId, DoDonationRequest request) {

        if (request.getDonationName() == null || request.getDonationName().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "기부처는 필수 입력값입니다.");
        }

        if (request.getPoint() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "point는 필수 입력값입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        Donation donation = donationRepository.findByDonationName(request.getDonationName())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "기부처를 찾을 수 없음: " + request.getDonationName()));

        if (user.getPoint() < request.getPoint()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "보유 point가 부족합니다.");
        }

        user.updatePoint(-request.getPoint());
        user.updateSumPoint(request.getPoint());
        user.updatePositiveness(request.getPoint() / 100);
        User savedUser = userRepository.save(user);

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;

        redisUtil.addSumPointToZSetWithTTL(rankKey, userKey, savedUser.getSumPoint());
    }


    /**
     * 기부처 구독을 토글하는 API
     *
     * @param userId  사용자 ID
     * @param donationId   구독을 토글할 기부처의 ID
     * @return 구독 상태 (true: 구독, false: 구독 취소)
     */
    @Override
    @Transactional
    public Boolean toggleDonationSubscribe(Long userId, Long donationId) {

        if (donationId == null || donationId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 기부처 ID입니다.");
        }

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 기부처를 찾을 수 없습니다. 기부처 ID: " + donationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        UserDonation existUserDonation = userDonationRepository.findByUserIdAndDonationId(userId, donationId)
                .orElse(null);

        if (existUserDonation != null) {
            // 기존 구독 관계가 있으면 구독 취소
            userDonationRepository.delete(existUserDonation);
            return false;
        } else {
            UserDonation userDonation = new UserDonation(user, donation, "USER");
            userDonationRepository.save(userDonation);
        }
        return true;
    }


    @Override
    public List<GetSubscribeDonationResponse> getSubscribeDonation(Long userId) {

        List<UserDonation> userDonations = userDonationRepository.findByUserIdAndRegistration(userId, "USER");

        return userDonations.stream()
                .map(userDonation -> {
                    Donation donation = userDonation.getDonation();
                    return new GetSubscribeDonationResponse(
                            donation.getId(), donation.getDonationName()
                    );
                })
                .collect(Collectors.toList());
    }


    /**
     * 기부처 검색 기능을 제공하는 메서드
     *
     * @param keyword 검색할 기부처 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색된 기부처 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> searchDonation(
            String keyword, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Donation> donationPage = donationRepository.findByDonationNameContaining(keyword, pageRequest);

        // 조회된 사용자 목록을 SearchDonationResponse DTO로 변환
        List<SearchDonationResponse> donations = donationPage.getContent().stream()
                .map(donation -> {
                    return new SearchDonationResponse(
                            donation.getId(),
                            donation.getDonationName()
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", donationPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("donations", donations);

        // 3. 성공 응답 생성 및 반환
        return result;
    }
}
