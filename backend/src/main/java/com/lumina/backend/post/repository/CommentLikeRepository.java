package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.CommentLike;
import com.lumina.backend.post.model.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);


    int countByCommentId(Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);


    @EntityGraph(attributePaths = {"comment"})
    Page<CommentLike> findByUserId(Long userId, Pageable pageable);
}
