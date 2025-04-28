package com.lumina.backend.category.repository;

import com.lumina.backend.category.model.entity.Category;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryName(String categoryName);

    @Query("SELECT c.id FROM Category c WHERE c.categoryName = :categoryName")
    Long findIdByCategoryName(@Param("categoryName") String categoryName);
}
