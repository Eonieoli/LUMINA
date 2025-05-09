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
     * 게시물을 업로드하는 메서드
     *
     * @param userId 게시물을 업로드한 사용자
     * @param request UploadPostRequest 요청 바디
     */
    @Override
    @Transactional
    public UploadPostResponse uploadPost(
            Long userId, UploadPostRequest request) throws IOException {

        ValidationUtil.validateRequiredField(request.getCategoryName(), "카테고리");
        ValidationUtil.validateRequiredField(request.getPostContent(), "게시물 내용");

        User user = findUtil.getUserById(userId);
        Category category = findUtil.getCategoryByCategoryName(request.getCategoryName());

        int appliedReward = aiService.textReward(user, request.getPostContent());
        Post post = createPost(user, category, appliedReward, request);
        Post savedPost = postRepository.save(post);

        savePostHashtags(request.getHashtag(), savedPost);

        return new UploadPostResponse(savedPost.getId());
    }


    @Override
    public Map<String, Object> getPosts(
            Long myId, Long userId, String categoryName, int pageNum) {

        ValidationUtil.validatePageNumber(pageNum);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = getPostPage(myId, userId, categoryName, pageRequest);

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> convertToPostResponse(post, myId))
                .collect(Collectors.toList());

        return PagingResponseUtil.toPagingResult(postPage, pageNum, "posts", posts);
    }


    /**
     * 특정 게시물을 삭제하는 메서드
     *
     * @param postId 삭제할 게시물의 ID
     * @param userId 삭제 요청을 한 사용자의 ID
     * @param role 삭제 요청을 한 사용자의 role
     */
    @Override
    @Transactional
    public void deletePost(
            Long userId, String role, Long postId) {

        Post post = findUtil.getPostById(postId);

        ValidationUtil.validatePostDelete(role, post, userId);

        postRepository.delete(post);
        if (post.getPostImage() != null) {
            s3Service.deleteImageFile(post.getPostImage(), "post/");
        }
    }


    /**
     * 게시물에 대한 좋아요 토글 메서드
     *
     * @param userId  사용자 ID
     * @param postId 게시물 ID
     * @return 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     */
    @Override
    @Transactional
    public Boolean toggleLike(Long userId, Long postId) {

        ValidationUtil.validateId(postId, "게시물");

        Post post = findUtil.getPostById(postId);
        User user = findUtil.getUserById(userId);

        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .map(existingPostLike -> {
                    postLikeRepository.delete(existingPostLike);
                    updateUserLike(user, -1);
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(new PostLike(user, post));
                    updateUserLike(user, 1);
                    return true;
                });
    }


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
     * 게시물 검색 기능을 제공하는 메서드
     *
     * @param keyword 검색할 게시물 텍스트
     * @return Map<String, Object> 검색된 게시물 목록을 포함한 응답
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


    private Post createPost(User user, Category category, int appliedReward, UploadPostRequest request) throws IOException {
        if (request.getPostImageFile() != null && !request.getPostImageFile().isEmpty()) {
            String postImage = s3Service.uploadImageFile(request.getPostImageFile(), "post/");
            return new Post(user, category, postImage, request.getPostContent(), 0, appliedReward);
        } else {
            return new Post(user, category, request.getPostContent(), 0, appliedReward);
        }
    }

    private void savePostHashtags(List<String> hashtags, Post post) {
        if (hashtags == null || hashtags.isEmpty()) return;

        for (String hashtagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName)));

            postHashtagRepository.save(new PostHashtag(post, hashtag));
        }
    }

    private Page<Post> getPostPage(Long myId, Long userId, String categoryName, PageRequest pageRequest) {
        if (userId != null) {
            return postRepository.findByUserId(userId, pageRequest);
        } else if (categoryName != null) {
            Long categoryId = categoryRepository.findIdByCategoryName(categoryName);
            return postRepository.findByCategoryId(categoryId, pageRequest);
        } else {
            List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(myId);
            followingIds.add(myId);
            return postRepository.findByUserIdIn(followingIds, pageRequest);
        }
    }

    private GetPostResponse convertToPostResponse(Post post, Long myId) {
        User user = post.getUser();
        Category category = post.getCategory();

        List<String> hashtagList = postHashtagRepository.findHashtagNamesByPostId(post.getId());
        int likeCnt = postLikeRepository.countByPostId(post.getId());
        int commentCnt = commentRepository.countByPostIdAndParentCommentIdIsNull(post.getId());
        Boolean isLike = postLikeRepository.existsByUserIdAndPostId(myId, post.getId());

        post.plusViews(1);  // 조회수 증가
        postRepository.save(post); // 트래픽 많으면 별도 처리 고려

        return new GetPostResponse(
                post.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                post.getPostImage(), post.getPostContent(), post.getPostViews(),
                category.getCategoryName(), hashtagList, likeCnt, commentCnt, isLike
        );
    }

    private void updateUserLike(User user, int value) {
        user.updateUserLikeCnt(value);
        userRepository.save(user);
    }
}
