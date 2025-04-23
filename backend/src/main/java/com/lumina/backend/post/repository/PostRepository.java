package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 특정 사용자가 올린 게시물 개수를 조회합니다.
     *
     * @param userId 게시물 사용자 ID
     * @return 팔로워 수
     */
    int countByUserId(Long userId);
}
