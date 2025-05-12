package com.lumina.backend.post.model.entity;

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
@Table(name = "comment")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(name = "comment_content", nullable = false, length = 300)
    private String commentContent;

    @Column(name = "comment_reward", nullable = false)
    private Integer commentReward;


    public Comment(User user, Post post, Comment parentComment, String commentContent, Integer commentReward) {
        this.user = user;
        this.post = post;
        this.parentComment = parentComment;
        this.commentContent = commentContent;
        this.commentReward = commentReward;
    }

    public Comment(User user, Post post, String commentContent, Integer commentReward) {
        this.user = user;
        this.post = post;
        this.commentContent = commentContent;
        this.commentReward = commentReward;
    }
}
