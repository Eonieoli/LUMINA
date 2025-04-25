package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    int countByPostId(Long postId);
}
