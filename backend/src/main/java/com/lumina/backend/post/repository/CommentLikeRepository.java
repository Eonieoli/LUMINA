package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    int countByCommentId(Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
}
