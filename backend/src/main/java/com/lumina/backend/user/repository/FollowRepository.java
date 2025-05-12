package com.lumina.backend.user.repository;

import com.lumina.backend.user.model.entity.Follow;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);


    int countByFollowingId(Long followingId);

    int countByFollowerId(Long followerId);

    Boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);


    @EntityGraph(attributePaths = {"follower"})
    List<Follow> findByFollowingId(Long followingId);

    @EntityGraph(attributePaths = {"following"})
    List<Follow> findByFollowerId(Long followerId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
    List<Long> findFollowingIdsByFollowerId(@Param("userId") Long userId);
}
