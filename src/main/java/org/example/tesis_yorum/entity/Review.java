package org.example.tesis_yorum.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Yorum Yazısı")
    @Size(min = 10, max = 1000, message = "Yorum 10 ila 1000 karakter arasında olmalıdır")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Min(value = 1, message = "Puanlama en az 1 olmalı")
    @Max(value = 5, message = "Puanlama en fazla 5 olmalı")
    @Column(nullable = false)
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)  // Changed to EAGER to avoid lazy loading issues
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "password"})  // Prevent circular reference
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)  // Changed to EAGER
    @JoinColumn(name = "facility_id", nullable = false)
    @JsonIgnoreProperties({"reviews"})  // Prevent circular reference
    private Facility facility;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.EAGER)  // Changed to EAGER
    @JsonIgnoreProperties({"review"})  // Prevent circular reference
    private List<FileAttachment> attachments = new ArrayList<>();

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Review() {}

    public Review(String content, Integer rating, User user, Facility facility) {
        this.content = content;
        this.rating = rating;
        this.user = user;
        this.facility = facility;
    }

    // Getters and Setters (same as before)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public ReviewStatus getStatus() {
        return status;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }


    // Helper methods
    public void approve(Long adminId) {
        this.status = ReviewStatus.APPROVED;
        this.approvedBy = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(Long adminId, String notes) {
        this.status = ReviewStatus.REJECTED;
        this.approvedBy = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", rating=" + rating +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}