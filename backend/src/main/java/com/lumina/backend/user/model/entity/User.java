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

    /**
     * 사용자 닉네임을 생성합니다.
     *
     * @param nickname      새로운 닉네임
     */
    public void createNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 사용자 탈퇴를 합니다.
     */
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

    /**
     * 사용자 프로필 정보를 업데이트합니다.
     *
     * @param nickname      새로운 닉네임
     * @param profileImage  새로운 프로필 이미지 URL
     * @param message       새로운 상태 메시지
     */
    public void updateProfile(String profileImage, String nickname, String message) {
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.message = message;
    }

    /**
     * 사용자의 포인트(리워드)를 업데이트합니다.
     *
     * @param point 추가할 포인트 값
     */
    public void updatePoint(Integer point) {
        this.point += point;
    }

    /**
     * 사용자의 누적포인트(리워드)를 업데이트합니다.
     *
     * @param point 추가할 포인트 값
     */
    public void updateSumPoint(Integer point) {
        this.sumPoint += point;
    }

    /**
     * 사용자의 선향 수치를 업데이트합니다.
     *
     * @param positiveness 추가할 선향 수치
     */
    public void updatePositiveness(Integer positiveness) {
        this.positiveness += positiveness;
        this.positiveness = Math.max(-100, Math.min(100, this.positiveness));
    }

    /**
     * 사용자의 상태(탈퇴 여부)를 업데이트합니다.
     *
     * @param userStatus true면 정상 회원, false면 탈퇴 상태
     */
    public void updateUserStatus(Boolean userStatus) {
        this.userStatus = userStatus;
    }

    public void updateUserLikeCnt(Integer likeCnt) {
        this.likeCnt += likeCnt;
    }

    public void resetUserLickCnt() {
        this.likeCnt = 0;
    }
}
