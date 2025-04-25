package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.CommentLike;
import com.lumina.backend.post.model.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    int countByCommentId(Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
}
