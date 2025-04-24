package com.lumina.backend.post.service.impl;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.model.entity.Hashtag;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.model.entity.PostHashtag;
import com.lumina.backend.post.model.request.UploadPostRequest;
import com.lumina.backend.post.repository.HashtagRepository;
import com.lumina.backend.post.repository.PostHashtagRepository;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.post.service.PostService;
import com.lumina.backend.post.service.S3Service;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;

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
}
