package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.PostHashtag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

    @Query("SELECT ph.hashtag.hashtagName FROM PostHashtag ph WHERE ph.post.id = :postId")
    List<String> findHashtagNamesByPostId(@Param("postId") Long postId);
}
