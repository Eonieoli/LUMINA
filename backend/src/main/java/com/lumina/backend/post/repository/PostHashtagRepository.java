package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
}
