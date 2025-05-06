package com.lumina.backend.user.repository;

import com.lumina.backend.user.model.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findBySocialId(String socialId);

    Optional<User> findByNickname(String nickname);


    Page<User> findByNicknameContaining(String keyword, Pageable pageable);


    @Query("SELECT u.Id FROM User u WHERE u.nickname = :nickname")
    Long findIdByNickname(@Param("nickname") String nickname);

    @Query("SELECT u.nickname FROM User u WHERE u.socialId = :socialId")
    String findNicknameBySocialId(@Param("socialId") String socialId);

    @Query("SELECT u.profileImage FROM User u WHERE u.id = :userId")
    String findProfileImageByUserId(@Param("userId") Long userId);

    @Query("SELECT u.role FROM User u WHERE u.socialId = :socialId")
    String findRoleBySocialId(@Param("socialId") String socialId);

    @Query("SELECT u.nickname FROM User u WHERE u.id = :userId")
    String findNicknameByUserId(@Param("userId") Long userId);
}
