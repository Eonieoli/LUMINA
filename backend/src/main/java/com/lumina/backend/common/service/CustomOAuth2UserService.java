package com.lumina.backend.common.service;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.user.model.dto.CustomOAuth2User;
import com.lumina.backend.user.model.dto.UserDto;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.response.GoogleResponse;
import com.lumina.backend.user.model.response.KakaoResponse;
import com.lumina.backend.user.model.response.OAuth2Response;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final RedisUtil redisUtil;


    /**
     * OAuth2 인증 후 사용자 정보를 로드합니다.
     *
     * @param userRequest OAuth2UserRequest 객체 (클라이언트 요청 정보 포함)
     * @return OAuth2User 객체 (사용자 정보 포함)
     * @throws OAuth2AuthenticationException 인증 실패 시 발생하는 예외
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        // OAuth2 제공자별 응답 파싱
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = parseOAuth2Response(registrationId, oAuth2User.getAttributes());

        // 기존 사용자 조회 또는 신규 회원 가입
        User user = userRepository.findBySocialId(oAuth2Response.getProviderId())
                .orElseGet(() -> registerNewUser(oAuth2Response, registrationId));

        UserDto userDto = new UserDto(oAuth2Response.getProviderId(), user.getNickname(), "ROLE_USER");

        return new CustomOAuth2User(userDto);
    }


    /**
     * OAuth2 제공자별 응답을 파싱합니다.
     *
     * @param registrationId 제공자 ID (google, kakao 등)
     * @param attributes 사용자 속성 정보
     * @return OAuth2Response 객체
     */
    private OAuth2Response parseOAuth2Response(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equals("google")) {
            return new GoogleResponse(attributes);
        }
        else if (registrationId.equals("kakao")) {
            return new KakaoResponse(attributes);
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다.");
    }

    /**
     * 신규 사용자를 등록합니다.
     *
     * @param oAuth2Response OAuth2 응답 정보
     * @param registrationId 제공자 ID
     * @return 저장된 User 엔티티
     */
    private User registerNewUser(OAuth2Response oAuth2Response, String registrationId) {
        User user = new User(
                oAuth2Response.getProviderId(),
                oAuth2Response.getProvider(),
                oAuth2Response.getProfileImage(),
                "상태메시지를 작성해주세요!",
                0, 0, 0, 0,
                "ROLE_USER",
                true
        );

        // 닉네임 생성 및 저장
        User savedUser = userRepository.save(user);
        String nickname = oAuth2Response.getName() + "_" + registrationId + savedUser.getId();
        savedUser.createNickname(nickname);
        userRepository.save(savedUser);

        // 랭킹 집합에 0점으로 추가
        redisUtil.addSumPointToZSetWithTTL("sum-point:rank", "user:" + savedUser.getId(), 0);

        return savedUser;
    }
}

