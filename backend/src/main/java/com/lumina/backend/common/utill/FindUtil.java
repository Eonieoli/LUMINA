package com.lumina.backend.common.utill;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.category.repository.CategoryRepository;
import com.lumina.backend.common.exception.CustomException;
import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.post.model.entity.Comment;
import com.lumina.backend.post.model.entity.Post;
import com.lumina.backend.post.repository.CommentRepository;
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
    private final CommentRepository commentRepository;
    private final DonationRepository donationRepository;


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

    public Comment getCommentById(Long commentId) {

        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "댓글을 찾을수 없음: " + commentId ));
    }

    public Donation getDonationById(Long donationId) {

        return donationRepository.findById(donationId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "기부처를 찾을 수 없음: " + donationId));
    }

    public Category getCategoryById(Long categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다. 카테고리 ID: " + categoryId));
    }
}

