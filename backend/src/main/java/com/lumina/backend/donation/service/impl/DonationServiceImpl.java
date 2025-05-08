package com.lumina.backend.donation.service.impl;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.PagingResponseUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.model.entity.UserDonation;
import com.lumina.backend.donation.model.response.GetDetailDonationResponse;
import com.lumina.backend.donation.model.response.GetDonationResponse;
import com.lumina.backend.donation.model.response.GetSubscribeDonationResponse;
import com.lumina.backend.donation.model.response.SearchDonationResponse;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.donation.repository.UserDonationRepository;
import com.lumina.backend.donation.service.DonationService;
import com.lumina.backend.post.model.entity.PostLike;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final UserDonationRepository userDonationRepository;
    private final UserRepository userRepository;

    private final RedisUtil redisUtil;
    private final FindUtil findUtil;


    @Override
    public List<GetDonationResponse> getDonation(Long userId) {

        return donationRepository.findByStatusTrue().stream()
                .map(donation -> mapToGetDonationResponse(userId, donation))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void doDonation(Long userId, DoDonationRequest request) {

        ValidationUtil.validateRequiredField(request.getDonationId(), "기부처");
        ValidationUtil.validateRequiredField(request.getPoint(), "포인트");

        User user = findUtil.getUserById(userId);
        Donation donation = findUtil.getDonationById(request.getDonationId());

        ValidationUtil.validateUserPoint(user, request.getPoint());

        processDonation(user, donation, request.getPoint());
        updateUserAfterDonation(user, request.getPoint());
        updateDonationRankingInRedis(userId, user.getSumPoint());
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

        ValidationUtil.validateId(donationId, "기부처");

        Donation donation = findUtil.getDonationById(donationId);
        User user = findUtil.getUserById(userId);

        return userDonationRepository.findByUserIdAndDonationIdAndRegistration(userId, donationId, "USER")
                .map(existUserDonation -> {
                    userDonationRepository.delete(existUserDonation);
                    return false;
                })
                .orElseGet(() -> {
                    userDonationRepository.save(new UserDonation(user, donation, "USER"));
                    return true;
                });
    }


    @Override
    public Map<String, Object> getSubscribeDonation(Long userId) {

        Map<String, Object> result = new HashMap<>();
        result.put("user", getDonationResponseList(userId, "USER"));
        result.put("ai", getDonationResponseList(userId, "AI"));

        return result;
    }


    /**
     * 기부처 검색 기능을 제공하는 메서드
     *
     * @param keyword 검색할 기부처 텍스트
     * @return ResponseEntity<BaseResponse<Map<String, Object>>> 검색된 기부처 목록을 포함한 응답
     */
    @Override
    public Map<String, Object> searchDonation(String keyword, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Donation> donationPage = donationRepository.findByDonationNameContaining(keyword, pageRequest);

        List<SearchDonationResponse> donations = donationPage.getContent().stream()
                .map(donation -> {
                    return new SearchDonationResponse(
                            donation.getId(),
                            donation.getDonationName()
                    );
                })
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(donationPage, pageNum, "donations", donations);
    }


    @Override
    public GetDetailDonationResponse getDetailDonation(Long userId, Long donationId) {

        ValidationUtil.validateId(donationId, "기부처");

        Donation donation = findUtil.getDonationById(donationId);
        UserDonation existUserDonation = userDonationRepository.findByUserIdAndDonationIdAndRegistration(userId, donationId, "DONATION")
                .orElse(null);

        int myDonationCnt = existUserDonation != null ? existUserDonation.getDonationCnt() : 0;
        int mySumDonation = existUserDonation != null ? existUserDonation.getDonationSum() : 0;
        Boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                userId, donationId, "USER");

        return new GetDetailDonationResponse(
                donationId, donation.getDonationName(), donation.getSumPoint(),
                donation.getSumUser(), myDonationCnt, mySumDonation, isSubscribe);
    }


    private GetDonationResponse mapToGetDonationResponse(Long userId, Donation donation) {
        boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                userId, donation.getId(), "USER");

        return new GetDonationResponse(
                donation.getId(),
                donation.getDonationName(),
                isSubscribe
        );
    }

    private void processDonation(User user, Donation donation, int point) {
        Optional<UserDonation> optionalUserDonation =
                userDonationRepository.findByUserIdAndDonationIdAndRegistration(user.getId(), donation.getId(), "DONATION");

        if (optionalUserDonation.isPresent()) {
            optionalUserDonation.get().updateUserDonation(point);
            donation.updateDonation(point, 0);
        } else {
            UserDonation newDonation = new UserDonation();
            newDonation.registerDonation(user, donation, point);
            userDonationRepository.save(newDonation);
            donation.updateDonation(point, 1);
        }

        donationRepository.save(donation);
    }

    private void updateUserAfterDonation(User user, int point) {
        user.updatePoint(-point);
        user.updateSumPoint(point);
        user.updatePositiveness(point / 100);
        userRepository.save(user);
    }

    private void updateDonationRankingInRedis(Long userId, int sumPoint) {
        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        redisUtil.addSumPointToZSetWithTTL(rankKey, userKey, sumPoint);
    }

    private List<GetSubscribeDonationResponse> getDonationResponseList(Long userId, String registration) {
        return userDonationRepository.findByUserIdAndRegistration(userId, registration).stream()
                .map(userDonation -> {
                    Donation donation = userDonation.getDonation();
                    return new GetSubscribeDonationResponse(
                            donation.getId(), donation.getDonationName()
                    );
                })
                .collect(Collectors.toList());
    }
}
