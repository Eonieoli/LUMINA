package com.lumina.backend.donation.service.impl;

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
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.donation.model.request.DoDonationRequest;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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


    /**
     * 모든 활성화된 기부처 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return Map<String, Object> 기부처 응답 리스트
     */
    @Override
    public Map<String, Object> getDonation(Long userId, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Donation> donationPage = donationRepository.findByStatusTrue(pageRequest);

        List<GetDonationResponse> donations = donationPage.getContent().stream()
                .map(donation -> convertToDonationResponse(donation, userId))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(donationPage, pageNum, "donations", donations);
    }


    /**
     * 실제 기부를 처리하는 메서드입니다.
     * 유효성 검사, 기부 처리, 유저 정보 갱신, 랭킹 갱신을 수행합니다.
     *
     * @param userId 사용자 ID
     * @param request DoDonationRequest 기부 요청 정보
     */
    @Override
    @Transactional
    public void doDonation(Long userId, DoDonationRequest request) {

        ValidationUtil.validateRequiredField(request.getDonationId(), "기부처");
        ValidationUtil.validateRequiredField(request.getPoint(), "포인트");

        User user = findUtil.getUserById(userId);
        Donation donation = findUtil.getDonationById(request.getDonationId());

        ValidationUtil.validateUserPoint(user, request.getPoint()); // 유저 포인트 초과 검증

        processDonation(user, donation, request.getPoint()); // 실제 기부 처리
        updateUserAfterDonation(user, request.getPoint()); // 기부 후 사용자 정보 처리
        updateDonationRankingInRedis(userId, user.getSumPoint()); // Redis에 기부 랭킹 정보 갱신
    }


    /**
     * 기부처 구독을 토글합니다.
     *
     * @param userId 사용자 ID
     * @param donationId 구독할 기부처 ID
     * @return Boolean 구독 상태 (true: 구독, false: 구독 취소)
     */
    @Override
    @Transactional
    public Boolean toggleDonationSubscribe(Long userId, Long donationId) {

        ValidationUtil.validateId(donationId, "기부처");

        Donation donation = findUtil.getDonationById(donationId);
        User user = findUtil.getUserById(userId);

        // 이미 구독 중이면 구독 취소, 아니면 구독 등록
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


    /**
     * 사용자의 구독/AI 추천 기부처 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return Map<String, Object> 구독 및 AI 추천 기부처 목록
     */
    @Override
    public Map<String, Object> getSubscribeDonation(Long userId) {

        Map<String, Object> result = new HashMap<>();
        result.put("user", getDonationResponseList(userId, "USER"));
        result.put("ai", getDonationResponseList(userId, "AI"));

        return result;
    }


    /**
     * 기부처 검색 기능을 제공합니다.
     *
     * @param keyword 검색어
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 검색 결과
     */
    @Override
    public Map<String, Object> searchDonation(String keyword, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (keyword == null || keyword.trim().isEmpty()) {
            return PagingResponseUtil.toPagingResult(
                    Page.empty(pageRequest), pageNum, "donations", Collections.emptyList()
            );
        }
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


    /**
     * 기부처 상세 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param donationId 기부처 ID
     * @return GetDetailDonationResponse 상세 정보 응답
     */
    @Override
    public GetDetailDonationResponse getDetailDonation(Long userId, Long donationId) {

        ValidationUtil.validateId(donationId, "기부처");

        Donation donation = findUtil.getDonationById(donationId);
        UserDonation existUserDonation = userDonationRepository.findByUserIdAndDonationIdAndRegistration(userId, donationId, "DONATION")
                .orElse(null);

        // 해당 기부처에 몇 번 기부했는지 반환
        int myDonationCnt = existUserDonation != null ? existUserDonation.getDonationCnt() : 0;
        // 해당 기부처에 얼마 기부했는지 반환
        int mySumDonation = existUserDonation != null ? existUserDonation.getDonationSum() : 0;
        Boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                userId, donationId, "USER");

        return new GetDetailDonationResponse(
                donationId, donation.getDonationName(), donation.getSumPoint(),
                donation.getSumUser(), myDonationCnt, mySumDonation, isSubscribe);
    }


    /**
     * 기부처 정보를 응답 객체로 변환합니다.
     *
     * @param userId 사용자 ID
     * @param donation Donation 엔티티
     * @return GetDonationResponse 변환된 응답 객체
     */
    private GetDonationResponse mapToGetDonationResponse(Long userId, Donation donation) {

        boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                userId, donation.getId(), "USER");

        return new GetDonationResponse(
                donation.getId(),
                donation.getDonationName(),
                isSubscribe
        );
    }

    /**
     * 실제 기부 내역을 처리합니다.
     *
     * @param user User 엔티티
     * @param donation Donation 엔티티
     * @param point 기부 포인트
     */
    private void processDonation(
            User user, Donation donation, int point) {

        Optional<UserDonation> optionalUserDonation =
                userDonationRepository.findByUserIdAndDonationIdAndRegistration(user.getId(), donation.getId(), "DONATION");

        if (optionalUserDonation.isPresent()) {
            // 기존 기부 내역이 있으면 업데이트
            optionalUserDonation.get().updateUserDonation(point);
            donation.updateDonation(point, 0);
        } else {
            // 처음 기부하는 경우 새로 등록
            UserDonation newDonation = new UserDonation();
            newDonation.registerDonation(user, donation, point);
            userDonationRepository.save(newDonation);
            donation.updateDonation(point, 1);
        }

        donationRepository.save(donation);
    }

    /**
     * 기부 후 사용자 정보를 갱신합니다.
     *
     * @param user User 엔티티
     * @param point 기부 포인트
     */
    private void updateUserAfterDonation(User user, int point) {

        user.updatePoint(-point); // 포인트 차감
        user.updateSumPoint(point); // 누적 포인트 증가
        user.updatePositiveness(point / 1000); // 긍정 지수 갱신
        userRepository.save(user);
    }
    /**
     * Redis에 기부 랭킹 정보를 갱신합니다.
     *
     * @param userId 사용자 ID
     * @param sumPoint 누적 포인트
     */
    private void updateDonationRankingInRedis(Long userId, int sumPoint) {

        String rankKey = "sum-point:rank";
        String userKey = "user:" + userId;
        redisUtil.addSumPointToZSetWithTTL(rankKey, userKey, sumPoint);
    }

    /**
     * 사용자별 구독/AI 추천 기부처 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param registration 구분 (USER/AI)
     * @return List<GetSubscribeDonationResponse> 구독/AI 추천 기부처 응답 리스트
     */
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

    /**
     * 기부처 정보를 응답 객체로 변환합니다.
     *
     * @param userId 사용자 ID
     * @param donation Donation 엔티티
     * @return GetDonationResponse 변환된 응답 객체
     */
    private GetDonationResponse convertToDonationResponse(Donation donation, Long userId) {

        boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                userId, donation.getId(), "USER");

        return new GetDonationResponse(
                donation.getId(), donation.getDonationName(), isSubscribe);
    }
}
