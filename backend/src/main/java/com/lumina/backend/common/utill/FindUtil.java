package com.lumina.backend.common.utill;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.repository.PostRepository;
import com.lumina.backend.user.model.entity.User;
import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FindUtil {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;


    public User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음: " + userId));
    }


    public Category getCategoryByCategoryName(String categoryName) {

        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없음: " + categoryName));
    }

    public Post getPostById(Long postId) {

        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시물을 찾을수 없음: " + postId ));
    }
}
