package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);


    int countByPostId(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);


    @EntityGraph(attributePaths = {"post"})
    Page<PostLike> findByUserId(Long userId, Pageable pageable);
}
