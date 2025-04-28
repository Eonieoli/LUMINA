package com.lumina.backend.admin.service.impl;

import com.lumina.backend.admin.model.response.GetUserCommentResponse;
import com.lumina.backend.admin.model.response.GetUserPostResponse;
import com.lumina.backend.admin.model.response.GetUserResponse;
import com.lumina.backend.admin.service.AdminService;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.repository.CommentRepository;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.post.service.PostService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final RedisUtil redisUtil;

    private final PostService postService;


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


    @Override
    public Map<String, Object> getCurUser(
            Long userId, int pageNum) {

        Boolean isAdmin = checkAdmin(userId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // Redis에서 접속자 키 조회
        Set<String> keys = redisUtil.getKeysByPattern("refresh:*:*");

        // 키에서 userId 추출
        Set<Long> userIds = keys.stream()
                .map(key -> {
                    String[] parts = key.split(":");
                    return Long.valueOf(parts[1]);
                })
                .collect(Collectors.toSet());

        // 페이징 처리
        List<Long> userIdList = new ArrayList<>(userIds);
        int pageSize = 10;
        int fromIndex = Math.max(0, (pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize, userIdList.size());
        List<Long> pagedUserIds = userIdList.subList(fromIndex, toIndex);

        // 해당 유저 조회
        List<User> users = userRepository.findAllById(pagedUserIds);

        // DTO 변환
        List<GetUserResponse> userResponses = users.stream()
                .map(user -> new GetUserResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getProfileImage(),
                        user.getMessage(),
                        user.getPoint(),
                        user.getSumPoint(),
                        user.getGrade(),
                        user.getPositiveness()
                ))
                .collect(Collectors.toList());

        // 6. 전체 페이지 계산
        int totalPages = (int) Math.ceil((double) userIds.size() / pageSize);

        // 7. 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", totalPages);
        result.put("currentPage", pageNum);
        result.put("users", userResponses);

        return result;
    }


    @Override
    public Map<String, Object> getUserPost(
            Long myId, Long userId, int pageNum) {

        Boolean isAdmin = checkAdmin(myId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByUserId(userId, pageRequest);

        List<GetUserPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    return new GetUserPostResponse(
                            post.getId(),
                            post.getPostImage(),
                            post.getPostContent(),
                            post.getPostViews()
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("posts", posts);

        return result;
    }


    @Override
    public Map<String, Object> getUserComment(
            Long myId, Long userId, int pageNum) {

        Boolean isAdmin = checkAdmin(myId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByUserId(userId, pageRequest);

        List<GetUserCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> {
                    return new GetUserCommentResponse(
                            comment.getId(),
                            comment.getPost().getId(),
                            comment.getCommentContent()
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", commentPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("comments", comments);

        return result;
    }


    @Override
    public void deletePost(Long userId, Long postId) {

        Boolean isAdmin = checkAdmin(userId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        postService.deletePost(userId, "ROLE_ADMIN", postId);
    }


    @Override
    public void deleteComment(Long userId, Long postId, Long commentId) {

        Boolean isAdmin = checkAdmin(userId);

        if (!isAdmin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        postService.deleteComment(userId, "ROLE_ADMIN", postId, commentId);
    }


    private Boolean checkAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));

        String role = user.getRole();

        return role.equals("ROLE_ADMIN");
    }
}
