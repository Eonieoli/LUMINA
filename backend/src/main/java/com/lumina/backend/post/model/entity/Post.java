package com.lumina.backend.post.model.entity;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.common.model.entity.BaseEntity;
import com.lumina.backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "post_image", length = 300)
    private String postImage;

    @Column(name = "post_content", nullable = false, length = 300)
    private String postContent;

    public Post (User user, Category category, String postImage, String postContent) {
        this.user = user;
        this.category = category;
        this.postImage = postImage;
        this.postContent = postContent;
    }

    public Post (User user, Category category, String postContent) {
        this.user = user;
        this.category = category;
        this.postContent = postContent;
    }
}
