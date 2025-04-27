package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 특정 사용자가 올린 게시물 개수를 조회합니다.
     *
     * @param userId 게시물 사용자 ID
     * @return 팔로워 수
     */
    int countByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findByUserIdIn(List<Long> userIds, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT ph.post FROM PostHashtag ph JOIN ph.hashtag h WHERE h.hashtagName LIKE %:keyword%")
    Page<Post> findPostsByHashtagName(@Param("keyword") String keyword, Pageable pageable);
}
