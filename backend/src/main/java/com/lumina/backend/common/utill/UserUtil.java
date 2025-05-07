package com.lumina.backend.common.utill;

import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {

    private final UserRepository userRepository;

    public User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));
    }
}
