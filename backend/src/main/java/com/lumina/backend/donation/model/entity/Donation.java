package com.lumina.backend.donation.model.entity;

import com.lumina.backend.category.model.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor
@Table(name = "donation")
public class Donation {


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

    public Donation(Category category, String donationName, Boolean status) {
        this.category = category;
        this.donationName = donationName;
        this.status = status;
    }

    public void changeStatus(Boolean status) {
        this.status = status;
    }
}
