package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    int countByPostId(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
