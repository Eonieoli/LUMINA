package com.lumina.backend.donation.model.entity;

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
@Table(name = "user_donation")
public class UserDonation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_donation_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @Column(name = "registration", nullable = false)
    private String registration;

    public UserDonation(User user, Donation donation, String registration) {
        this.user = user;
        this.donation = donation;
        this.registration = registration;
    }
}
