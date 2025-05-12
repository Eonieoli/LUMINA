package com.lumina.backend.post.model.entity;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.common.model.entity.BaseEntity;
import com.lumina.backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
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

    @Column(name = "post_views", nullable = false)
    private Integer postViews;

    @Column(name = "post_reward", nullable = false)
    private Integer postReward;


    public Post (User user, Category category, String postImage, String postContent, Integer postViews, Integer postReward) {
        this.user = user;
        this.category = category;
        this.postImage = postImage;
        this.postContent = postContent;
        this.postViews = postViews;
        this.postReward = postReward;
    }

    public Post (User user, Category category, String postContent, Integer postViews, Integer postReward) {
        this.user = user;
        this.category = category;
        this.postContent = postContent;
        this.postViews = postViews;
        this.postReward = postReward;
    }

    public void plusViews(Integer postViews) {
        this.postViews += postViews;
    }
}
