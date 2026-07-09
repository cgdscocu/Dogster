package com.dogster.sittingpost;

import com.dogster.pet.Pet;
import com.dogster.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sitting_posts")
public class SittingPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SittingPostStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_sitter_id")
    private UserAccount assignedSitter;

    protected SittingPost() {
    }

    public SittingPost(
            UserAccount owner,
            Pet pet,
            String description,
            double latitude,
            double longitude
    ) {
        this.owner = owner;
        this.pet = pet;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = SittingPostStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public Pet getPet() {
        return pet;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public SittingPostStatus getStatus() {
        return status;
    }

    public UserAccount getAssignedSitter() {
        return assignedSitter;
    }

    public void assignTo(UserAccount sitter) {
        this.assignedSitter = sitter;
        this.status = SittingPostStatus.ASSIGNED;
    }

    public void close() {
        this.status = SittingPostStatus.CLOSED;
    }
}
