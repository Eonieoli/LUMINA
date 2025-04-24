package com.lumina.backend.post.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.model.entity.Hashtag;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostHashtag;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.model.response.GetPostResponse;
import com.lumina.backend.post.repository.*;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.post.service.S3Service;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
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

    private final S3Service s3Service;


    /**
     * 게시물을 업로드하는 메서드
     *
     * @param userId 게시물을 업로드한 사용자
     * @param request UploadPostRequest 요청 바디
     */
    @Override
    @Transactional
    public void uploadPost(
            Long userId, UploadPostRequest request) throws IOException {

        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "카테고리은 필수 입력값입니다.");
        }

        if (request.getPostContent() == null || request.getPostContent().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "게시물 내용은 필수 입력값입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId ));

        Category category = categoryRepository.findByCategoryName(request.getCategoryName())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없음: " + request.getCategoryName()));

        Post post;
        if (request.getPostImageFile() != null && !request.getPostImageFile().isEmpty()) {
            String postImage = s3Service.uploadImageFile(request.getPostImageFile(), "post/");
            post = new Post(user, category, postImage, request.getPostContent());
        } else {
            post = new Post(user, category, request.getPostContent());
        }
        postRepository.save(post);

        // 해시태그 처리
        if (request.getHashtag() != null && !request.getHashtag().isEmpty()) {
            for (String hashtagName : request.getHashtag()) {
                // 1. 해시태그 존재 여부 확인
                Hashtag hashtag = hashtagRepository.findByHashtagName(hashtagName)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(hashtagName))); // 없으면 저장

                // 2. PostHashtag 연결 저장
                PostHashtag postHashtag = new PostHashtag(post, hashtag);
                postHashtagRepository.save(postHashtag);
            }
        }
    }


    @Override
    public Map<String, Object> getPosts(Long myId, Long userId, String categoryName, int pageNum) {

        if (pageNum < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "페이지 번호는 1 이상의 값이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage;

        if (userId != null) {
            // 특정 유저 게시물 조회
            postPage = postRepository.findByUserId(userId, pageRequest);

        } else if (categoryName != null) {
            // 특정 카테고리 게시물 조회
            Long categoryId = categoryRepository.findIdByCategoryName(categoryName);
            postPage = postRepository.findByCategoryId(categoryId, pageRequest);

        } else {
            // 전체 게시물 조회
            postPage = postRepository.findAll(pageRequest);
        }

        List<GetPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    User user = post.getUser();
                    Category category = post.getCategory();
                    List<String> hashtagList = postHashtagRepository.findHashtagNamesByPostId(post.getId());
                    int likeCnt = postLikeRepository.countByPostId(post.getId());
                    int commentCnt = commentRepository.countByPostId(post.getId());
                    Boolean isLike = postLikeRepository.existsByUserIdAndPostId(myId, post.getId());

                    return new GetPostResponse(
                            post.getId(), user.getId(), user.getNickname(), user.getProfileImage(),
                            post.getPostImage(), post.getPostContent(), category.getCategoryName(),
                            hashtagList, likeCnt, commentCnt, isLike
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("posts", posts);

        return result;
    }
}
