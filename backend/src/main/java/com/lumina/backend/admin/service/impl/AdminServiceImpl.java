package com.lumina.backend.admin.service.impl;

import com.lumina.backend.admin.model.response.GetUserResponse;
import com.lumina.backend.admin.service.AdminService;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.model.response.SearchUserResponse;
import com.lumina.backend.user.repository.UserRepository;
import com.lumina.backend.user.service.OAuthService;
import com.lumina.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final RedisUtil redisUtil;


    @Override
    public Map<String, Object> getUser(
            Long userId, int pageNum) {

        Boolean isAdmin = checkAdmin(userId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findAll(pageRequest);

        List<GetUserResponse> users = userPage.getContent().stream()
                .map(user -> {
                    return new GetUserResponse(
                            user.getId(),
                            user.getNickname(),
                            user.getProfileImage(),
                            user.getMessage(),
                            user.getPoint(),
                            user.getSumPoint(),
                            user.getGrade(),
                            user.getPositiveness()
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", userPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("users", users);

        return result;
    }


    @Override
    public void deleteUser(
            Long myId, Long userId) {

        Boolean isAdmin = checkAdmin(myId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId));

        user.deleteUser();
        userRepository.save(user);

        // Redis에서 rank 삭제
        String rankKey = "sum-point:rank";
        String rankUserKey = "user:" + userId;
        redisUtil.removeUserFromZSet(rankKey, rankUserKey);

        // Redis에서 Refresh Token 삭제
        String userKeyMobile = "refresh:" + userId + ":mobile";
        String userKeyPc = "refresh:" + userId + ":pc";
        redisUtil.delete(userKeyMobile);
        redisUtil.delete(userKeyPc);
    }


    private Boolean checkAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        String role = user.getRole();

        return role.equals("ROLE_ADMIN");
    }
}
