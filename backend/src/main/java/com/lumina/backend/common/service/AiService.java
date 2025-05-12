package com.lumina.backend.common.service;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.lumina.model.response.GetRewardResponse;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final UserRepository userRepository;

    private final WebClient webClient;

    @Value("${AI_SERVER}")
    private String aiServer;


    /**
     * 사용자의 텍스트에 대한 보상을 계산하고, 사용자 정보(포인트/긍정지수)를 업데이트합니다.
     *
     * @param user 보상을 받을 사용자
     * @param text 평가할 텍스트
     * @return 실제 적용된 보상값 (양수: 포인트 증가, 음수: 긍정지수 감소)
     */
    @Transactional
    public int textReward(User user, String text) {

        // AI 서버로부터 reward 값 요청
        int reward = requestReward(Map.of("text", text)).getReward();
        int appliedReward;

        if (reward >= 0) {
            // 긍정지수 또는 최소값(10)으로 보상 배수 결정
            int multiplier = Math.max(user.getPositiveness(), 10);
            appliedReward = reward * multiplier;
            user.updatePoint(appliedReward);
        } else {
            appliedReward = reward;
            user.updatePositiveness(reward);
        }

        userRepository.save(user);
        return appliedReward;
    }


    /**
     * AI 서버에 텍스트 평가 요청을 보내고, 보상 결과를 반환합니다.
     *
     * @param requestPayload 평가 요청에 필요한 데이터
     * @return GetRewardResponse AI 서버의 응답 객체
     * @throws CustomException 평가 서버 응답이 없을 때 발생
     */
    private GetRewardResponse requestReward(Map<String, String> requestPayload) {

        GetRewardResponse response = webClient.post()
                .uri(aiServer)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(GetRewardResponse.class)
                .block(); // 동기 호출

        if (response == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "평가 서버에서 응답이 없습니다.");
        }

        return response;
    }
}
