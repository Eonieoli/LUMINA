package com.lumina.backend.user.model.entity;

import com.lumina.backend.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "social_id", nullable = false ,length = 50)
    private String socialId;

    @Column(name = "social_type", nullable = false)
    private String socialType;

    @Column(name = "nickname", nullable = true, length = 20)
    private String nickname;

    @Column(name = "profile_image", nullable = true, length = 300)
    private String profileImage;

    @Column(name = "message", nullable = true, length = 30)
    private String message;

    @Column(name = "point", nullable = false)
    private Integer point;

    @Column(name = "sum_point", nullable = false)
    private Integer sumPoint;

    @Column(name = "positiveness", nullable = false)
    private Integer positiveness;

    @Column(name = "like_cnt", nullable = false)
    private Integer likeCnt;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "user_status", nullable = false)
    private Boolean userStatus;


    public User(String socialId, String socialType, String profileImage, String message, Integer point,
                Integer sumPoint, Integer positiveness, Integer likeCnt, String role, Boolean userStatus) {
        this.socialId = socialId;
        this.socialType = socialType;
        this.profileImage = profileImage;
        this.message = message;
        this.point = point;
        this.sumPoint = sumPoint;
        this.positiveness = positiveness;
        this.likeCnt = likeCnt;
        this.role = role;
        this.userStatus = userStatus;
    }

    public void createNickname(String nickname) {
        this.nickname = nickname;
    }

    public void deleteUser() {
        this.socialId = "-";
        this.profileImage = null;
        this.nickname = "(알수없음)";
        this.message = null;
        this.point = -1;
        this.sumPoint = -1;
        this.positiveness = -101;
        this.userStatus = false;
    }

    public void updateProfile(String profileImage, String nickname, String message) {
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.message = message;
    }

    public void updatePoint(Integer point) {
        this.point += point;
    }

    public void updateSumPoint(Integer point) {
        this.sumPoint += point;
    }

    public void updatePositiveness(Integer positiveness) {
        this.positiveness += positiveness;
        this.positiveness = Math.max(-100, Math.min(100, this.positiveness));
    }

    public void updateUserLikeCnt(Integer likeCnt) {

        this.likeCnt += likeCnt;
        this.likeCnt = Math.max(0, this.likeCnt);
    }

    public void resetUserLickCnt() {
        this.likeCnt = 0;
    }
}
