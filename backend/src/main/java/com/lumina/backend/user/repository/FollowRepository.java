package com.lumina.backend.user.repository;

import com.lumina.backend.user.model.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 팔로우(Follow) 관련 DB 접근을 담당하는 레포지토리
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * 특정 사용자를 팔로우하는 사람의 수(팔로워 수)를 조회합니다.
     *
     * @param followingId 팔로우 대상 사용자 ID
     * @return 팔로워 수
     */
    int countByFollowingId(Long followingId);

    /**
     * 특정 사용자가 팔로우하고 있는 사람의 수(팔로잉 수)를 조회합니다.
     *
     * @param followerId 팔로우 요청을 한 사용자 ID
     * @return 팔로잉 수
     */
    int countByFollowerId(Long followerId);

    /**
     * 특정 팔로우 관계가 존재하는지 여부를 확인합니다.
     *
     * @param followerId 팔로우 요청을 한 사용자 ID
     * @param followingId 팔로우 대상 사용자 ID
     * @return 팔로우 관계가 존재하면 true, 아니면 false
     */
    Boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
