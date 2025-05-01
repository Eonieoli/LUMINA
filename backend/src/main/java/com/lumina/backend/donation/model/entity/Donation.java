package com.lumina.backend.donation.model.entity;

import com.lumina.backend.category.model.entity.Category;
import com.lumina.backend.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor
@Table(name = "donation")
public class Donation extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "donation_name", nullable = false, length = 30)
    private String donationName;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "sum_point", nullable = false)
    private Integer sumPoint;

    @Column(name = "sum_user", nullable = false)
    private Integer sumUser;

    public Donation(Category category, String donationName, Boolean status, Integer sumPoint, Integer sumUser) {
        this.category = category;
        this.donationName = donationName;
        this.status = status;
        this.sumPoint = sumPoint;
        this.sumUser = sumUser;
    }

    public void changeStatus(Boolean status) {
        this.status = status;
    }

    public void updateDonation(Integer sumPoint, Integer user) {
        this.sumPoint += sumPoint;
        this.sumUser += user;
    }
}
