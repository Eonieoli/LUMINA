package com.lumina.backend.post.model.entity;

import com.lumina.backend.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor
@Table(name = "hashtag")
public class Hashtag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id", nullable = false)
    private Long id;

    @Column(name = "hashtag_name", nullable = false, length = 10)
    private String hashtagName;


    public Hashtag(String hashtagName) {
        this.hashtagName = hashtagName;
    }
}
