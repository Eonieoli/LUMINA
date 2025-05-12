package com.lumina.backend.post.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.category.repository.UserCategoryRepository;
import com.lumina.backend.common.service.AiService;
import com.lumina.backend.common.service.S3Service;
import com.lumina.backend.common.utill.FindUtil;
import com.lumina.backend.common.utill.PagingResponseUtil;
import com.lumina.backend.common.utill.ValidationUtil;
import com.lumina.backend.post.model.entity.*;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.*;
import com.lumina.backend.post.repository.*;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.FollowRepository;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final UserCategoryRepository userCategoryRepository;

    private final S3Service s3Service;
    private final AiService aiService;

    private final FindUtil findUtil;


    /**
     * 게시물을 업로드합니다.
     *
     * @param userId  업로드한 사용자 ID
     * @param request 게시물 업로드 요청 DTO
     * @return UploadPostResponse 업로드된 게시물 ID 응답
     */
    @Override
    @Transactional
    public UploadPostResponse uploadPost(
            Long userId, UploadPostRequest request) throws IOException {

        ValidationUtil.validateRequiredField(request.getCategoryName(), "카테고리");
        ValidationUtil.validateRequiredField(request.getPostContent(), "게시물 내용");

        User user = findUtil.getUserById(userId);
        Category category = findUtil.getCategoryByCategoryName(request.getCategoryName());

        int appliedReward = aiService.textReward(user, request.getPostContent()); // AI 기반 보상 산출
        Post post = createPost(user, category, appliedReward, request);
        Post savedPost = postRepository.save(post);

        savePostHashtags(request.getHashtag(), savedPost); // 해시태그 저장

        return new UploadPostResponse(savedPost.getId());
    }


    /**
     * 게시물 목록을 조회합니다.
     *
     * @param myId         현재 사용자 ID
     * @param userId       특정 사용자 게시물만 조회 시 ID
     * @param categoryName 카테고리명으로 필터링 시
     * @param feedType     팔로잉 or 전체 게시물
     * @param pageNum      페이지 번호
     * @return Map<String, Object> 페이징된 게시물 목록
     */
    @Override
    public Map<String, Object> getPosts(
            Long myId, Long userId, String categoryName,
            String feedType, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);
        ValidationUtil.validateRequiredField(feedType, "피드타입");

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = getPostPage(myId, userId, categoryName, feedType, pageRequest);

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> convertToPostResponse(post, myId))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(postPage, pageNum, "posts", posts);
    }


    /**
     * 게시물 삭제
     *
     * @param userId 삭제 요청자 ID
     * @param role   삭제 요청자 권한
     * @param postId 삭제할 게시물 ID
     */
    @Override
    @Transactional
    public void deletePost(
            Long userId, String role, Long postId) {

        Post post = findUtil.getPostById(postId);

        ValidationUtil.validatePostDelete(role, post, userId); // 삭제 권한 검증

        postRepository.delete(post);
        if (post.getPostImage() != null) {
            s3Service.deleteImageFile(post.getPostImage(), "post/"); // S3 이미지 삭제
        }
    }


    /**
     * 게시물 좋아요 토글
     *
     * @param userId 사용자 ID
     * @param postId 게시물 ID
     * @return 좋아요 true/취소 false
     */
    @Override
    @Transactional
    public Boolean toggleLike(Long userId, Long postId) {

        ValidationUtil.validateId(postId, "게시물");

        Post post = findUtil.getPostById(postId);
        User user = findUtil.getUserById(userId);

        // 이미 좋아요 했으면 취소, 아니면 추가
        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .map(existingPostLike -> {
                    postLikeRepository.delete(existingPostLike);
                    updateUserLike(user, -1); // 사용자 좋아요 수 감소
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(new PostLike(user, post));
                    updateUserLike(user, 1); // 사용자 좋아요 수 증가
                    return true;
                });
    }


    /**
     * 사용자가 구독한 카테고리의 게시물 목록 조회
     *
     * @param userId  사용자 ID
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 게시물 목록
     */
    @Override
    public Map<String, Object> getSubscribePost(Long userId, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        List<Long> subscribedCategoryIds = userCategoryRepository.findCategoryIdsByUserId(userId);
        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByCategoryIdIn(subscribedCategoryIds, pageRequest);

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> convertToPostResponse(post, userId))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(postPage, pageNum, "posts", posts);
    }


    /**
     * 게시물 해시태그 검색
     *
     * @param userId  사용자 ID
     * @param keyword 검색어(해시태그)
     * @param pageNum 페이지 번호
     * @return Map<String, Object> 페이징된 게시물 목록
     */
    @Override
    public Map<String, Object> searchPost(
            Long userId, String keyword, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findPostsByHashtagName(keyword, pageRequest);

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> convertToPostResponse(post, userId))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(postPage, pageNum, "posts", posts);
    }


    /**
     * 게시물 엔티티 생성
     *
     * @param user          게시글 작성자
     * @param category      카테고리
     * @param appliedReward AI 산출 보상
     * @param request       업로드 요청 DTO
     * @return Post 생성된 게시물 엔티티
     */
    private Post createPost(
            User user, Category category, int appliedReward,
            UploadPostRequest request) throws IOException {

        // 이미지가 있으면 S3 업로드 후 경로 저장, 없으면 텍스트만 저장
        if (request.getPostImageFile() != null && !request.getPostImageFile().isEmpty()) {
            String postImage = s3Service.uploadImageFile(request.getPostImageFile(), "post/");
            return new Post(user, category, postImage, request.getPostContent(), 0, appliedReward);
        } else {
            return new Post(user, category, request.getPostContent(), 0, appliedReward);
        }
    }

    /**
     * 게시물의 해시태그 저장
     *
     * @param hashtags 해시태그 문자열 리스트
     * @param post     게시물 엔티티
     */
    private void savePostHashtags(List<String> hashtags, Post post) {

        if (hashtags == null || hashtags.isEmpty()) return;

        for (String hashtagName : hashtags) {
            // 기존 해시태그 없으면 생성
            Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName)));

            postHashtagRepository.save(new PostHashtag(post, hashtag));
        }
    }

    /**
     * 게시물 목록 조회 쿼리 분기
     *
     * @param myId         현재 사용자 ID
     * @param userId       특정 사용자 게시물만 조회 시
     * @param categoryName 카테고리명으로 필터링 시
     * @param pageRequest  페이지 요청 객체
     * @return Page<Post>  페이징된 게시물 목록
     */
    private Page<Post> getPostPage(
            Long myId, Long userId, String categoryName,
            String feedType, PageRequest pageRequest) {

        if (userId != null) {
            return postRepository.findByUserId(userId, pageRequest);
        } else if (categoryName != null) {
            Long categoryId = categoryRepository.findIdByCategoryName(categoryName);
            return postRepository.findByCategoryId(categoryId, pageRequest);
        } else {
            // 팔로잉 사용자 + 본인 게시물 조회
            List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(myId);
            followingIds.add(myId);
            if (feedType.equals("following")) {
                return postRepository.findByUserIdIn(followingIds, pageRequest);
            } else {
                return postRepository.findByUserIdNotIn(followingIds, pageRequest);
            }
        }
    }

    /**
     * 게시물 정보를 응답 DTO로 변환
     *
     * @param post 게시물 엔티티
     * @param myId 현재 사용자 ID(좋아요 여부 확인용)
     * @return GetPostResponse 응답 DTO
     */
    private GetPostResponse convertToPostResponse(Post post, Long myId) {

        User user = post.getUser();
        Category category = post.getCategory();

        List<String> hashtagList = postHashtagRepository.findHashtagNamesByPostId(post.getId());
        int likeCnt = postLikeRepository.countByPostId(post.getId());
        int commentCnt = commentRepository.countByPostIdAndParentCommentIdIsNull(post.getId());
        Boolean isLike = postLikeRepository.existsByUserIdAndPostId(myId, post.getId());

        post.plusViews(1);  // 조회수 증가
        postRepository.save(post);

        return new GetPostResponse(
                post.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                post.getPostImage(), post.getPostContent(), post.getPostViews(),
                category.getCategoryName(), hashtagList, likeCnt, commentCnt, isLike
        );
    }

    /**
     * 사용자 좋아요 수 갱신 및 저장
     *
     * @param user  대상 사용자
     * @param value 증감 값 (+1, -1)
     */
    private void updateUserLike(User user, int value) {
        user.updateUserLikeCnt(value);
        userRepository.save(user);
    }
}
