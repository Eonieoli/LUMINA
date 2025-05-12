package com.lumina.backend.category.repository;

import com.lumina.backend.category.model.entity.UserCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {

    Optional<UserCategory> findByUserIdAndCategoryId(Long userId, Long categoryId);


    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);


    @Query("SELECT uc.category.id FROM UserCategory uc WHERE uc.user.id = :userId")
    List<Long> findCategoryIdsByUserId(@Param("userId") Long userId);
}
