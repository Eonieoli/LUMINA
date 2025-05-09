package com.lumina.backend.user.repository;

import com.lumina.backend.user.model.entity.Follow;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);


    int countByFollowingId(Long followingId);

    int countByFollowerId(Long followerId);

    Boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);


    @Query(value = "SELECT f FROM Follow f WHERE f.following.id = :followingId",
            countQuery = "SELECT COUNT(f) FROM Follow f WHERE f.following.id = :followingId")
    Page<Follow> findByFollowingId(@Param("followingId") Long followingId, Pageable pageable);
    List<Follow> findByFollowingId(Long followingId);

    @Query(value = "SELECT f FROM Follow f WHERE f.follower.id = :followerId",
            countQuery = "SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :followerId")
    Page<Follow> findByFollowerId(@Param("followerId") Long followerId, Pageable pageable);
    List<Follow> findByFollowerId(Long followerId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
    List<Long> findFollowingIdsByFollowerId(@Param("userId") Long userId);
}
