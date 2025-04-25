package com.lumina.backend.user.repository;

import com.lumina.backend.user.model.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 소셜 ID로 사용자 엔티티를 조회합니다.
     *
     * @param socialId 소셜 로그인 ID
     * @return User 엔티티
     */
    Optional<User> findBySocialId(String socialId);

    /**
     * 닉네임으로 사용자 ID를 조회합니다.
     *
     * @param nickname 닉네임
     * @return 사용자 ID
     */
    @Query("SELECT u.Id FROM User u WHERE u.nickname = :nickname")
    Long findIdByNickname(@Param("nickname") String nickname);

    /**
     * 소셜 ID로 닉네임을 조회합니다.
     *
     * @param socialId 소셜 로그인 ID
     * @return 닉네임
     */
    @Query("SELECT u.nickname FROM User u WHERE u.socialId = :socialId")
    String findNicknameBySocialId(@Param("socialId") String socialId);

    /**
     * 닉네임으로 사용자 엔티티를 조회합니다.
     *
     * @param nickname 닉네임
     * @return User 엔티티
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 사용자 ID로 프로필 이미지를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 프로필 이미지 URL
     */
    @Query("SELECT u.profileImage FROM User u WHERE u.id = :userId")
    String findProfileImageByUserId(@Param("userId") Long userId);

    /**
     * 소셜 ID로 Role을 조회합니다.
     *
     * @param socialId 소셜 로그인 ID
     * @return role
     */
    @Query("SELECT u.role FROM User u WHERE u.socialId = :socialId")
    String findRoleBySocialId(@Param("socialId") String socialId);
}
