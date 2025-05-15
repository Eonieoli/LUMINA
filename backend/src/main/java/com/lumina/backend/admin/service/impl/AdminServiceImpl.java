package com.lumina.backend.admin.service.impl;

import com.lumina.backend.admin.model.response.GetUserCommentResponse;
import com.lumina.backend.admin.model.response.GetUserPostResponse;
import com.lumina.backend.admin.model.response.GetUserResponse;
import com.lumina.backend.admin.service.AdminService;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.PagingResponseUtil;
import com.lumina.backend.common.utill.RedisUtil;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.repository.CommentRepository;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.post.service.CommentService;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final FindUtil findUtil;

    private final PostService postService;
    private final CommentService commentService;


    /**
     * 전체 유저 목록을 페이징하여 조회합니다.
     * 관리자 권한 체크가 필요합니다.
     *
     * @param userId  관리자 ID
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 유저 목록
     */
    @Override
    public Map<String, Object> getUser(Long userId, int pageNum) {

        checkAdmin(userId);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findAll(pageRequest);

        List<GetUserResponse> users = userRepository.findAll(pageRequest).getContent().stream()
                .map(user -> {
                    return new GetUserResponse(
                            user.getId(), user.getNickname(), user.getProfileImage(),
                            user.getMessage(), user.getPoint(), user.getSumPoint(),
                            user.getPositiveness()
                    );
                })
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(userPage, pageNum, "users", users);
    }


    /**
     * 유저를 삭제(비활성화)합니다.
     * 관리자 권한 체크가 필요합니다.
     * 관련된 Redis 정보도 삭제합니다.
     *
     * @param myId   관리자 ID
     * @param userId 삭제할 유저 ID
     */
    @Override
    public void deleteUser(Long myId, Long userId) {

        checkAdmin(myId);

        User user = findUtil.getUserById(userId);
        user.deleteUser();
        userRepository.save(user);

        // 랭킹, 리프레시 토큰 등 Redis 정보 삭제
        redisUtil.removeUserFromZSet("sum-point:rank", "user:" + userId);
        redisUtil.delete("refresh:" + userId + ":mobile");
        redisUtil.delete("refresh:" + userId + ":pc");
    }


    /**
     * 현재 로그인 중인 유저 목록을 Redis에서 추출하여 페이징 조회합니다.
     * 관리자 권한 체크가 필요합니다.
     *
     * @param userId  관리자 ID
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 유저 목록
     */
    @Override
    public Map<String, Object> getCurUser(Long userId, int pageNum) {

        checkAdmin(userId);

        Set<Long> userIds = extractUserIdsFromRedis();
        List<Long> pagedUserIds = paginateUserIds(userIds, pageNum);

        List<GetUserResponse> userResponses = userRepository.findAllById(pagedUserIds).stream()
                .map(user -> new GetUserResponse(
                        user.getId(), user.getNickname(), user.getProfileImage(),
                        user.getMessage(), user.getPoint(), user.getSumPoint(),
                        user.getPositiveness()
                ))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) userIds.size() / 10);

        return PagingResponseUtil.toPagingResult(totalPages, pageNum, "users", userResponses);
    }


    /**
     * 특정 유저가 작성한 게시글 목록을 페이징 조회합니다.
     * 관리자 권한 체크가 필요합니다.
     *
     * @param myId    관리자 ID
     * @param userId  대상 유저 ID
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 게시글 목록
     */
    @Override
    public Map<String, Object> getUserPost(
            Long myId, Long userId, int pageNum) {

        checkAdmin(myId);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByUserId(userId, pageRequest);

        List<GetUserPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    return new GetUserPostResponse(
                            post.getId(), post.getPostImage(),
                            post.getPostContent(), post.getPostViews()
                    );
                })
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(postPage, pageNum, "posts", posts);
    }


    /**
     * 특정 유저가 작성한 댓글 목록을 페이징 조회합니다.
     * 관리자 권한 체크가 필요합니다.
     *
     * @param myId    관리자 ID
     * @param userId  대상 유저 ID
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 댓글 목록
     */
    @Override
    public Map<String, Object> getUserComment(
            Long myId, Long userId, int pageNum) {

        checkAdmin(myId);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByUserId(userId, pageRequest);

        List<GetUserCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> {
                    return new GetUserCommentResponse(
                            comment.getId(), comment.getPost().getId(),
                            comment.getCommentContent()
                    );
                })
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(commentPage, pageNum, "comments", comments);
    }


    /**
     * 게시글을 삭제합니다.
     * 관리자 권한 체크가 필요합니다.
     *
     * @param userId 관리자 ID
     * @param postId 삭제할 게시글 ID
     */
    @Override
    public void deletePost(Long userId, Long postId) {

        checkAdmin(userId);

        postService.deletePost(userId, "ROLE_ADMIN", postId);
    }


    /**
     * 댓글을 삭제합니다.
     * 관리자 권한 체크가 필요합니다.
     *
     * @param userId    관리자 ID
     * @param postId    게시글 ID
     * @param commentId 삭제할 댓글 ID
     */
    @Override
    public void deleteComment(Long userId, Long postId, Long commentId) {

        checkAdmin(userId);

        commentService.deleteComment(userId, "ROLE_ADMIN", postId, commentId);
    }


    /**
     * 관리자 권한을 체크합니다.
     * 권한이 없으면 예외를 발생시킵니다.
     *
     * @param userId 관리자 ID
     */
    private void checkAdmin(Long userId) {

        User user = findUtil.getUserById(userId);
        String role = user.getRole();
        if (!role.equals("ROLE_ADMIN")) {
            throw new CustomException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }
    }


    /**
     * Redis에서 현재 로그인 중인 유저 ID 목록을 추출합니다.
     *
     * @return Set<Long> 유저 ID 집합
     */
    private Set<Long> extractUserIdsFromRedis() {

        Set<String> keys = redisUtil.getKeysByPattern("refresh:*:*");
        // 키 형식: refresh:{userId}:{device}
        return keys.stream()
                .map(key -> {
                    String[] parts = key.split(":");
                    return Long.valueOf(parts[1]);
                })
                .collect(Collectors.toSet());
    }

    /**
     * 유저 ID 집합을 페이지 단위로 분할합니다.
     *
     * @param userIds 유저 ID 집합
     * @param pageNum 페이지 번호
     * @return List<Long> 페이징된 유저 ID 리스트
     */
    private List<Long> paginateUserIds(Set<Long> userIds, int pageNum) {

        List<Long> userIdList = new ArrayList<>(userIds);
        int pageSize = 10;
        int fromIndex = Math.max(0, (pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize, userIdList.size());
        return userIdList.subList(fromIndex, toIndex);
    }
}
