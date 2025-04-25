package com.lumina.backend.post.repository;

import com.lumina.backend.post.model.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByHashtagName(String hashtagName);
}
