package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    int countByPostId(Long postId);

    @EntityGraph(attributePaths = {"user", "parentComment"})
    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);

    int countByParentCommentId(Long commentId);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByPostIdAndParentCommentId(Long postId, Long parentCommentId, Pageable pageable);
}
