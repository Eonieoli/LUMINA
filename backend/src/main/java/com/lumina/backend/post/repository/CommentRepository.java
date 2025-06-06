package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    int countByPostIdAndParentCommentIdIsNull(Long postId);

    int countByParentCommentId(Long commentId);


    @EntityGraph(attributePaths = {"user", "parentComment"})
    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"post"})
    Page<Comment> findByUserId(Long userId, Pageable pageable);
    @EntityGraph(attributePaths = {"post"})
    List<Comment> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByPostIdAndParentCommentId(Long postId, Long parentCommentId);
}
