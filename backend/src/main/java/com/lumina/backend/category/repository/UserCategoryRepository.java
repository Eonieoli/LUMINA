package com.lumina.backend.category.repository;

import com.lumina.backend.category.model.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {

    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);

    Optional<UserCategory> findByUserIdAndCategoryId(Long userId, Long categoryId);
}
